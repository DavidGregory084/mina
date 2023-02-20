package org.mina_lang.gradle;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import org.gradle.api.Buildable;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.artifacts.dependencies.DefaultExternalModuleDependency;
import org.gradle.api.internal.file.collections.FailingFileCollection;
import org.gradle.api.internal.file.collections.LazilyInitializedFileCollection;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.internal.tasks.TaskDependencyResolveContext;
import org.gradle.api.plugins.jvm.internal.JvmEcosystemUtilities;

public abstract class MinaRuntime {
    private final ProjectInternal project;
    private final JvmEcosystemUtilities jvmEcosystemUtilities;
    private static final Pattern MINA_JAR_PATTERN = Pattern.compile("mina-(\\w.*?)-(\\d.*).jar");

    public MinaRuntime(Project project) {
        this.project = (ProjectInternal) project;
        this.jvmEcosystemUtilities = ((ProjectInternal) project).getServices().get(JvmEcosystemUtilities.class);
    }

    public FileCollection inferMinaClasspath(final Iterable<File> classpath) {
        return new LazilyInitializedFileCollection(project.getTaskDependencyFactory()) {
            @Override
            public String getDisplayName() {
                return "Mina runtime classpath";
            }

            @Override
            public FileCollection createDelegate() {
                try {
                    return inferMinaClasspath();
                } catch (RuntimeException e) {
                    return new FailingFileCollection(getDisplayName(), e);
                }
            }

            private FileCollection inferMinaClasspath() {
                File minaRuntimeJar = findMinaJar(classpath, "runtime");

                if (minaRuntimeJar == null) {
                    throw new GradleException(
                            String.format("Cannot infer Mina class path because no Mina library Jar was found. "
                                    + "Does %s declare dependency to mina-runtime? Searched classpath: %s.", project,
                                    classpath));
                }

                String minaVersion = getMinaVersion(minaRuntimeJar);

                if (minaVersion == null) {
                    throw new AssertionError(
                            String.format("Unexpectedly failed to parse version of Mina Jar file: %s in %s",
                                    minaRuntimeJar, project));
                }

                Configuration minaRuntimeClasspath = project.getConfigurations()
                        .detachedConfiguration(getMinaCompilerDependency(minaVersion));
                jvmEcosystemUtilities.configureAsRuntimeClasspath(minaRuntimeClasspath);

                return minaRuntimeClasspath;
            }

            private DefaultExternalModuleDependency getMinaCompilerDependency(String minaVersion) {
                return new DefaultExternalModuleDependency("org.mina-lang", "mina-compiler-main", minaVersion);
            }

            @Override
            public void visitDependencies(TaskDependencyResolveContext context) {
                if (classpath instanceof Buildable) {
                    context.add(classpath);
                }
            }
        };
    }

    @Nullable
    private static File findMinaJar(Iterable<File> classpath, String appendix) {
        for (File file : classpath) {
            Matcher matcher = MINA_JAR_PATTERN.matcher(file.getName());
            if (matcher.matches() && matcher.group(1).equals(appendix)) {
                return file;
            }
        }
        return null;
    }

    @Nullable
    private static String getMinaVersion(File minaJar) {
        Matcher matcher = MINA_JAR_PATTERN.matcher(minaJar.getName());
        return matcher.matches() ? matcher.group(2) : null;
    }
}
