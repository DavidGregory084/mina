package org.mina_lang.gradle;

import javax.inject.Inject;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.internal.tasks.DefaultSourceSet;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.internal.plugins.DslObject;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.plugins.internal.JvmPluginsHelper;
import org.gradle.api.plugins.jvm.internal.JvmPluginServices;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.util.internal.GUtil;

public class MinaBasePlugin implements Plugin<Project> {

    private JvmPluginServices jvmPluginServices;
    private ObjectFactory objectFactory;

    @Inject
    public MinaBasePlugin(JvmPluginServices jvmPluginServices, ObjectFactory objectFactory) {
        this.jvmPluginServices = jvmPluginServices;
        this.objectFactory = objectFactory;
    }

    public void apply(Project project) {
        project.getPluginManager().apply(JavaBasePlugin.class);
        configureSourceSets(project);
    }

    public void configureSourceSets(Project project) {
        project.getExtensions()
                .getByType(JavaPluginExtension.class)
                .getSourceSets()
                .all(sourceSet -> {
                    String displayName = sourceSet instanceof DefaultSourceSet
                            ? ((DefaultSourceSet) sourceSet).getDisplayName()
                            : GUtil.toWords(sourceSet.getName());

                    DefaultMinaSourceSet minaSourceSet = new DefaultMinaSourceSet(displayName, objectFactory);
                    new DslObject(sourceSet).getConvention().getPlugins().put("mina", minaSourceSet);
                    sourceSet.getExtensions().add(MinaSourceDirectorySet.class, "mina", minaSourceSet.getMina());

                    SourceDirectorySet minaSource = minaSourceSet.getMina();
                    minaSource.srcDir("src/" + sourceSet.getName() + "/mina");
                    sourceSet.getResources().getFilter().exclude(fileElem -> minaSourceSet.getMina().contains(fileElem.getFile()));
                    sourceSet.getAllSource().source(minaSource);

                    TaskProvider<MinaCompile> compileTask = project.getTasks()
                            .register(sourceSet.getCompileTaskName("mina"), MinaCompile.class, compile -> {
                                JvmPluginsHelper.configureForSourceSet(sourceSet, minaSource, compile,
                                        compile.getOptions(), project);
                                compile.setDescription("Compiles the " + displayName + " Mina source.");
                                compile.setSource(minaSource);
                            });

                    jvmPluginServices.useDefaultTargetPlatformInference(
                            project.getConfigurations().getByName(sourceSet.getCompileClasspathConfigurationName()),
                            compileTask);

                    jvmPluginServices.useDefaultTargetPlatformInference(
                            project.getConfigurations().getByName(sourceSet.getRuntimeClasspathConfigurationName()),
                            compileTask);

                    JvmPluginsHelper.configureOutputDirectoryForSourceSet(sourceSet, minaSource, project, compileTask,
                            compileTask.map(MinaCompile::getOptions));

                    project.getTasks().named(sourceSet.getClassesTaskName(), task -> task.dependsOn(compileTask));
                });
    }
}
