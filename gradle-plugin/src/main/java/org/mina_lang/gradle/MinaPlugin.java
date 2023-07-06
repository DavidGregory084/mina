/*
 * SPDX-FileCopyrightText:  © 2022 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
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
