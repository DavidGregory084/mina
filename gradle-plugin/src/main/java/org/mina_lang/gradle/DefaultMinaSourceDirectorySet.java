package org.mina_lang.gradle;

import javax.inject.Inject;

import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.internal.file.DefaultSourceDirectorySet;
import org.gradle.api.internal.tasks.TaskDependencyFactory;

public class DefaultMinaSourceDirectorySet extends DefaultSourceDirectorySet implements MinaSourceDirectorySet {
    @Inject
    public DefaultMinaSourceDirectorySet(SourceDirectorySet sourceSet, TaskDependencyFactory taskDependencyFactory) {
        super(sourceSet, taskDependencyFactory);
    }
}
