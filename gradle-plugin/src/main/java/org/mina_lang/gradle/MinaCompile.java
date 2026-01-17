/*
 * SPDX-FileCopyrightText:  © 2022-2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.gradle;

import org.gradle.api.NonNullApi;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileTree;
import org.gradle.api.internal.file.FileOperations;
import org.gradle.api.internal.tasks.compile.HasCompileOptions;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;
import org.gradle.api.tasks.compile.AbstractCompile;
import org.gradle.api.tasks.compile.CompileOptions;
import org.gradle.api.tasks.compile.ForkOptions;
import org.gradle.internal.execution.history.OutputsCleaner;
import org.gradle.internal.file.Deleter;
import org.gradle.internal.file.FileType;
import org.gradle.jvm.toolchain.JavaLauncher;
import org.gradle.workers.WorkQueue;
import org.gradle.workers.WorkerExecutor;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;

@CacheableTask
@NonNullApi
public abstract class MinaCompile extends AbstractCompile implements HasCompileOptions {

    private CompileOptions compileOptions;
    private FileCollection minaCompilerClasspath;
    private FileOperations fileOperations;
    private Deleter deleter;

    @Inject
    public MinaCompile(FileOperations fileOperations, Deleter deleter) {
        this.compileOptions = getObjectFactory().newInstance(CompileOptions.class);
        this.fileOperations = fileOperations;
        this.deleter = deleter;
    }

    @Nested
    public abstract MinaCompileOptions getMinaCompileOptions();

    @Classpath
    public FileCollection getMinaCompilerClasspath() {
        return minaCompilerClasspath;
    }

    public void setMinaCompilerClasspath(FileCollection minaCompilerClasspath) {
        this.minaCompilerClasspath = minaCompilerClasspath;
    }

    @Nested
    public CompileOptions getOptions() {
        return compileOptions;
    }

    @Nested
    public abstract Property<JavaLauncher> getJavaLauncher();

    @Inject
    public abstract ObjectFactory getObjectFactory();

    @Inject
    public abstract WorkerExecutor getWorkerExecutor();

    @Override
    @SkipWhenEmpty
    @IgnoreEmptyDirectories
    @PathSensitive(PathSensitivity.RELATIVE)
    @InputFiles
    public FileTree getSource() {
        return super.getSource();
    }

    private void cleanupStaleOutputFiles() {
        var classFilesPattern = fileOperations.patternSet()
            .include("**/*.class");

        var filesToDelete = getDestinationDirectory()
            .getAsFileTree()
            .matching(classFilesPattern)
            .getFiles();

        var destinationDir = getDestinationDirectory().getAsFile().get();

        var prefix = destinationDir.getAbsolutePath() + File.separator;

        OutputsCleaner outputsCleaner = new OutputsCleaner(
            deleter,
            // Only delete files in the destination dir
            file -> file.getAbsolutePath().startsWith(prefix),
            // Don't delete the destination dir itself
            dir -> !destinationDir.equals(dir)
        );

        try {
            for (File fileToDelete: filesToDelete) {
                if (fileToDelete.isFile()) {
                    outputsCleaner.cleanupOutput(fileToDelete, FileType.RegularFile);
                }
            }
            outputsCleaner.cleanupDirectories();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to clean up stale class files", e);
        }
    }

    @TaskAction
    public void compile() {
        cleanupStaleOutputFiles();

        WorkQueue workQueue = getOptions().isFork() ? getWorkerExecutor().processIsolation(spec -> {
            spec.getClasspath().from(getMinaCompilerClasspath());
            spec.forkOptions(opts -> {
                ForkOptions forkOpts = getOptions().getForkOptions();
                opts.setWorkingDir(getProject().getLayout().getProjectDirectory());
                opts.setExecutable(getJavaLauncher().get().getExecutablePath().getAsFile().getAbsolutePath());
                opts.setJvmArgs(forkOpts.getJvmArgs());
                opts.setMinHeapSize(forkOpts.getMemoryInitialSize());
                opts.setMaxHeapSize(forkOpts.getMemoryMaximumSize());
            });
        }) : getWorkerExecutor().classLoaderIsolation(spec -> {
            spec.getClasspath().from(getMinaCompilerClasspath());
        });

        workQueue.submit(MinaCompileAction.class, params -> {
            params.getCompilerClassName().set("org.mina_lang.cli.MinaCommandLine");
            params.getMinaCompileOptions().set(getMinaCompileOptions());
            params.getDestinationDirectory().set(getDestinationDirectory());
            params.getClasspath().from(getClasspath());
            params.getSourceFiles().from(getSource());
        });
    }
}
