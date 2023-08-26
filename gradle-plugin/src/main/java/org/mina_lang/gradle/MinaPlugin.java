/*
 * SPDX-FileCopyrightText:  © 2022-2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.gradle;

import com.opencastsoftware.gradle.bsp.BspExtension;
import com.opencastsoftware.gradle.bsp.BspPlugin;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;

public class MinaPlugin implements Plugin<Project> {
    public static final String MINA_LANGUAGE_ID = "mina";

    public void apply(Project project) {
        project.getPluginManager().apply(MinaBasePlugin.class);
        project.getPluginManager().apply(JavaPlugin.class);
        project.getPluginManager().apply(BspPlugin.class);
        var bspExtension = project.getExtensions().getByType(BspExtension.class);
        bspExtension.getSupportedLanguages().add(MINA_LANGUAGE_ID);
        bspExtension.getLanguageModelBuilders().add(new BspMinaLanguageModelBuilder());
    }
}
