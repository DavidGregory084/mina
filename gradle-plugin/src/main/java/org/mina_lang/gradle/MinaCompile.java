/*
 * SPDX-FileCopyrightText:  © 2022-2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.gradle;

import groovy.lang.Closure;
import org.gradle.api.Action;
import org.gradle.api.NonNullApi;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileTree;
import org.gradle.api.internal.file.FileOperations;
import org.gradle.api.internal.tasks.compile.HasCompileOptions;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.reporting.Reporting;
import org.gradle.api.tasks.*;
import org.gradle.api.tasks.compile.AbstractCompile;
import org.gradle.api.tasks.compile.CompileOptions;
import org.gradle.api.tasks.compile.ForkOptions;
import org.gradle.internal.Describables;
import org.gradle.internal.execution.history.OutputsCleaner;
import org.gradle.internal.file.Deleter;
import org.gradle.internal.file.FileType;
import org.gradle.jvm.toolchain.JavaLauncher;
import org.gradle.util.internal.ClosureBackedAction;
import org.gradle.workers.WorkQueue;
import org.gradle.workers.WorkerExecutor;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;

@CacheableTask
@NonNullApi
public abstract class MinaCompile extends AbstractCompile implements HasCompileOptions, Reporting<MinaCompileReportContainer> {

    private final CompileOptions compileOptions;
    private final ConfigurableFileCollection minaCompilerClasspath;
    private final MinaCompileReportContainer reports;
    private final FileOperations fileOperations;
    private final Deleter deleter;

    @Inject
    public MinaCompile(Project project, FileOperations fileOperations, Deleter deleter) {
        this.compileOptions = getObjectFactory().newInstance(CompileOptions.class);
        Configuration minacConfig = project.getConfigurations().getByName(MinaBasePlugin.MINAC_CONFIGURATION_NAME);
        this.minaCompilerClasspath = getObjectFactory().fileCollection().from(minacConfig.getAsFileTree());
        this.reports = getObjectFactory().newInstance(DefaultMinaCompileReportContainer.class, Describables.quoted("Task", getIdentityPath()));
        this.fileOperations = fileOperations;
        this.deleter = deleter;
    }

    @Nested
    public abstract MinaCompileOptions getMinaCompileOptions();

    @Classpath
    public FileCollection getMinaCompilerClasspath() {
        return minaCompilerClasspath;
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

    @Nested
    @Override
    public MinaCompileReportContainer getReports() {
        return reports;
    }

    @Override
    public MinaCompileReportContainer reports(Closure closure) {
        return reports(new ClosureBackedAction<MinaCompileReportContainer>(closure));
    }

    @Override
    public MinaCompileReportContainer reports(Action<? super MinaCompileReportContainer> configureAction) {
        configureAction.execute(reports);
        return reports;
    }

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
        var reportFilesPattern = fileOperations.patternSet()
            .include("**/*.html");

        var classFiles = getDestinationDirectory().getAsFileTree().matching(classFilesPattern);
        var reportFiles = getReports().getHtml().getOutputLocation().getAsFileTree().matching(reportFilesPattern);

        var filesToDelete = classFiles.plus(reportFiles).getFiles();

        var destinationDir = getDestinationDirectory().getAsFile().get();
        var reportsDir = getReports().getHtml().getOutputLocation().getAsFile().get();

        var classFilesPrefix = destinationDir.getAbsolutePath() + File.separator;
        var reportFilesPrefix = reportsDir.getAbsolutePath() + File.separator;

        OutputsCleaner outputsCleaner = new OutputsCleaner(
            deleter,
            // Only delete files in the destination and compile report dirs
            file -> file.getAbsolutePath().startsWith(classFilesPrefix) || file.getAbsolutePath().startsWith(reportFilesPrefix),
            // Don't delete the destination or report directories
            dir -> !(destinationDir.equals(dir) || reportsDir.equals(dir))
        );

        try {
            for (File fileToDelete : filesToDelete) {
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
            params.getReportingEnabled().set(getReports().getHtml().getRequired());
            params.getReportDirectory().set(getReports().getHtml().getOutputLocation());
            params.getClasspath().from(getClasspath());
            params.getSourceFiles().from(getSource());
        });
    }
}
