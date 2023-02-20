package org.mina_lang.gradle;

import static org.gradle.api.internal.lambdas.SerializableLambdas.spec;

import java.util.concurrent.Callable;

import javax.inject.Inject;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.ConventionMapping;
import org.gradle.api.internal.plugins.DslObject;
import org.gradle.api.internal.tasks.DefaultSourceSet;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.plugins.internal.JvmPluginsHelper;
import org.gradle.api.plugins.jvm.internal.JvmEcosystemUtilities;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.jvm.toolchain.JavaLauncher;
import org.gradle.jvm.toolchain.JavaToolchainService;
import org.gradle.workers.internal.KeepAliveMode;

public class MinaBasePlugin implements Plugin<Project> {

    public static final String MINA_RUNTIME_EXTENSION_NAME = "minaRuntime";

    private final ObjectFactory objectFactory;
    private final JvmEcosystemUtilities jvmEcosystemUtilities;

    @Inject
    public MinaBasePlugin(ObjectFactory objectFactory, JvmEcosystemUtilities jvmEcosystemUtilities) {
        this.objectFactory = objectFactory;
        this.jvmEcosystemUtilities = jvmEcosystemUtilities;
    }

    public void apply(Project project) {
        project.getPluginManager().apply(JavaBasePlugin.class);
        MinaRuntime minaRuntime = project.getExtensions().create(
                MINA_RUNTIME_EXTENSION_NAME, MinaRuntime.class, project);
        configureCompileDefaults(project, minaRuntime);
        configureSourceSetDefaults(project);
    }

    private void configureCompileDefaults(Project project, MinaRuntime minaRuntime) {
        project.getTasks().withType(MinaCompile.class).configureEach(compile -> {
            ConventionMapping conventionMapping = compile.getConventionMapping();
            conventionMapping.map("minaClasspath",
                    (Callable<FileCollection>) () -> minaRuntime.inferMinaClasspath(compile.getClasspath()));
            compile.getMinaCompileOptions().getIsFork().convention(false);
            compile.getMinaCompileOptions().getKeepAliveMode().convention(KeepAliveMode.SESSION);
        });
    }

    private void configureSourceSetDefaults(Project project) {
        javaPluginExtension(project).getSourceSets().all(sourceSet -> {
            MinaSourceDirectorySet minaSource = objectFactory.newInstance(DefaultMinaSourceDirectorySet.class,
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
                sourceSet.getCompileTaskName("mina"),
                MinaCompile.class, minaCompile -> {
                    JvmPluginsHelper.configureForSourceSet(sourceSet, minaSource, minaCompile, minaCompile.getOptions(), project);
                    minaCompile.setDescription("Compiles the " + minaSource + ".");
                    minaCompile.setSource(minaSource);
                    minaCompile.getJavaLauncher().convention(getJavaLauncher(project));
                });

        JvmPluginsHelper.configureOutputDirectoryForSourceSet(
                sourceSet, minaSource, project, compileTask,
                compileTask.map(AbstractMinaCompile::getOptions));

        jvmEcosystemUtilities.useDefaultTargetPlatformInference(
                project.getConfigurations().getByName(sourceSet.getCompileClasspathConfigurationName()), compileTask);
        jvmEcosystemUtilities.useDefaultTargetPlatformInference(
                project.getConfigurations().getByName(sourceSet.getRuntimeClasspathConfigurationName()), compileTask);

        project.getTasks().named(sourceSet.getClassesTaskName(), task -> task.dependsOn(compileTask));
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
