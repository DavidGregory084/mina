/*
 * SPDX-FileCopyrightText:  © 2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.gradle;

import com.opencastsoftware.gradle.bsp.BspLanguageModelBuilder;
import org.gradle.api.Project;
import org.gradle.api.tasks.SourceSet;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;

public class BspMinaLanguageModelBuilder extends BspLanguageModelBuilder {
    @Override
    public String getLanguageId() {
        return MinaPlugin.MINA_LANGUAGE_ID;
    }

    @Override
    public boolean isEnabledFor(SourceSet sourceSet) {
        return sourceSet.getExtensions().findByType(MinaSourceDirectorySet.class) != null;
    }

    @Nullable @Override
    protected String getDisplayNameFor(Project project, SourceSet sourceSet) {
        return sourceSet.getExtensions()
                .getByType(MinaSourceDirectorySet.class)
                .getDisplayName();
    }

    @Nullable @Override
    protected String getBuildTargetDataKindFor(Project project, SourceSet sourceSet) {
        return "jvm";
    }

    @Nullable @Override
    protected Serializable getBuildTargetDataFor(Project project, SourceSet sourceSet) {
        return getJvmBuildTargetFor(project);
    }
}
