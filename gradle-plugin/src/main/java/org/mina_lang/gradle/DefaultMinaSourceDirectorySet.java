package org.mina_lang.gradle;

import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.internal.file.DefaultSourceDirectorySet;

public class DefaultMinaSourceDirectorySet extends DefaultSourceDirectorySet implements MinaSourceDirectorySet {
    public DefaultMinaSourceDirectorySet(SourceDirectorySet sourceSet) {
        super(sourceSet);
    }
}
