/*
 * SPDX-FileCopyrightText:  © 2022-2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.internal.artifacts.dependencies.DefaultExternalModuleDependency;
import org.gradle.api.internal.tasks.DefaultSourceSet;
import org.gradle.api.internal.tasks.DefaultSourceSetOutput;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.plugins.jvm.internal.JvmEcosystemUtilities;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.compile.AbstractCompile;
import org.gradle.internal.Cast;
import org.gradle.jvm.toolchain.JavaLauncher;
import org.gradle.jvm.toolchain.JavaToolchainService;

import javax.inject.Inject;
import java.util.concurrent.Callable;

import static org.gradle.api.internal.lambdas.SerializableLambdas.spec;

public class MinaBasePlugin implements Plugin<Project> {
    private final ObjectFactory objectFactory;
    private final JvmEcosystemUtilities jvmEcosystemUtilities;

    public static final String MINAC_CONFIGURATION_NAME = "minac";
    public static final String MINA_EXTENSION_NAME = "mina";

    @Inject
    public MinaBasePlugin(ObjectFactory objectFactory, JvmEcosystemUtilities jvmEcosystemUtilities) {
        this.objectFactory = objectFactory;
        this.jvmEcosystemUtilities = jvmEcosystemUtilities;
    }

    public void apply(Project project) {
        project.getPluginManager().apply(JavaBasePlugin.class);

        MinaExtension minaExtension = project.getExtensions()
                .create(MinaExtension.class, MINA_EXTENSION_NAME, DefaultMinaExtension.class);

        configureConfigurations(project, minaExtension);
        configureSourceSetDefaults(project);
        configureCompileDefaults(project);
    }

    private void configureConfigurations(Project project, MinaExtension minaExtension) {
        Configuration minacConfiguration = project.getConfigurations().create(MINAC_CONFIGURATION_NAME);
        minacConfiguration.setCanBeConsumed(false);
        minacConfiguration.setCanBeResolved(true);
        jvmEcosystemUtilities.configureAsRuntimeClasspath(minacConfiguration);
        minacConfiguration.getDependencies().addLater(minaExtension.getMinaVersion().map(version -> {
            return new DefaultExternalModuleDependency("org.mina-lang", "mina-compiler", version);
        }));
    }

    private void configureCompileDefaults(Project project) {
        project.getTasks().withType(MinaCompile.class).configureEach(compile -> {
        });
    }

    private void configureSourceSetDefaults(Project project) {
        javaPluginExtension(project).getSourceSets().forEach(sourceSet -> {
            MinaSourceDirectorySet minaSource = objectFactory.newInstance(
                    DefaultMinaSourceDirectorySet.class,
                    objectFactory.sourceDirectorySet("mina",
                            ((DefaultSourceSet) sourceSet).getDisplayName() + " Mina source"));
            sourceSet.getExtensions().add(MinaSourceDirectorySet.class, "mina", minaSource);
            minaSource.srcDir(project.file("src/" + sourceSet.getName() + "/mina"));

            FileCollection minaSourceFiles = minaSource;
            sourceSet.getResources().getFilter().exclude(
                    spec(element -> minaSourceFiles.contains(element.getFile())));
            sourceSet.getAllSource().source(minaSource);

            createMinaCompileTask(project, sourceSet, minaSource);
        });
    }

    private void createMinaCompileTask(Project project, SourceSet sourceSet, MinaSourceDirectorySet minaSource) {
        TaskProvider<MinaCompile> compileTask = project.getTasks().register(
                sourceSet.getCompileTaskName("mina"), MinaCompile.class, minaCompile -> {
                    configureForSourceSet(sourceSet, minaSource, minaCompile, project);
                    minaCompile.setSource(minaSource);
                    minaCompile.getJavaLauncher().convention(getJavaLauncher(project));
                });

        configureOutputDirectoryForSourceSet(sourceSet, minaSource, project, compileTask);

        jvmEcosystemUtilities.useDefaultTargetPlatformInference(
                project.getConfigurations().getByName(sourceSet.getCompileClasspathConfigurationName()), compileTask);
        jvmEcosystemUtilities.useDefaultTargetPlatformInference(
                project.getConfigurations().getByName(sourceSet.getRuntimeClasspathConfigurationName()), compileTask);

        project.getTasks().named(sourceSet.getClassesTaskName(), task -> task.dependsOn(compileTask));
    }

    /** Inlined from JvmPluginsHelper */
    private static void configureForSourceSet(final SourceSet sourceSet, final SourceDirectorySet sourceDirectorySet,
            AbstractCompile compile, final Project target) {
        compile.setDescription("Compiles the " + sourceDirectorySet.getDisplayName() + ".");
        compile.setSource(sourceDirectorySet);

        ConfigurableFileCollection classpath = compile.getProject().getObjects().fileCollection();
        classpath.from((Callable<Object>) () -> sourceSet.getCompileClasspath()
                .plus(target.files(sourceSet.getJava().getClassesDirectory())));

        compile.getConventionMapping().map("classpath", () -> classpath);
    }

    /** Inlined from JvmPluginsHelper */
    public static void configureOutputDirectoryForSourceSet(final SourceSet sourceSet,
            final SourceDirectorySet sourceDirectorySet, final Project target,
            TaskProvider<? extends AbstractCompile> compileTask) {
        final String sourceSetChildPath = "classes/" + sourceDirectorySet.getName() + "/" + sourceSet.getName();
        sourceDirectorySet.getDestinationDirectory()
                .convention(target.getLayout().getBuildDirectory().dir(sourceSetChildPath));

        DefaultSourceSetOutput sourceSetOutput = Cast.cast(DefaultSourceSetOutput.class, sourceSet.getOutput());
        sourceSetOutput.getClassesDirs().from(sourceDirectorySet.getDestinationDirectory()).builtBy(compileTask);
        sourceDirectorySet.compiledBy(compileTask, AbstractCompile::getDestinationDirectory);
    }

    private static Provider<JavaLauncher> getJavaLauncher(Project project) {
        JavaPluginExtension extension = javaPluginExtension(project);
        JavaToolchainService service = extensionOf(project, JavaToolchainService.class);
        return service.launcherFor(extension.getToolchain());
    }

    private static JavaPluginExtension javaPluginExtension(Project project) {
        return extensionOf(project, JavaPluginExtension.class);
    }

    private static <T> T extensionOf(ExtensionAware extensionAware, Class<T> type) {
        return extensionAware.getExtensions().getByType(type);
    }
}
