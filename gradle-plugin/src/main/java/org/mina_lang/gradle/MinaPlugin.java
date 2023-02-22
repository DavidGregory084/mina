package org.mina_lang.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;

public class MinaPlugin implements Plugin<Project> {
    public void apply(Project project) {
        project.getPluginManager().apply(MinaBasePlugin.class);
        project.getPluginManager().apply(JavaPlugin.class);
    }
}
