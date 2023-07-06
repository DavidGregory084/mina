/*
 * SPDX-FileCopyrightText:  © 2022-2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.gradle;

import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.internal.file.DefaultSourceDirectorySet;
import org.gradle.api.internal.tasks.TaskDependencyFactory;

import javax.inject.Inject;

public class DefaultMinaSourceDirectorySet extends DefaultSourceDirectorySet implements MinaSourceDirectorySet {
    @Inject
    public DefaultMinaSourceDirectorySet(SourceDirectorySet sourceSet, TaskDependencyFactory taskDependencyFactory) {
        super(sourceSet, taskDependencyFactory);
    }
}
