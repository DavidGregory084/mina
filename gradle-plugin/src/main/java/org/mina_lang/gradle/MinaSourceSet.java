package org.mina_lang.gradle;

import org.gradle.api.Action;
import org.gradle.api.file.SourceDirectorySet;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;

public interface MinaSourceSet {
    MinaSourceDirectorySet getMina();

    @SuppressWarnings("rawtypes")
    MinaSourceSet mina(@DelegatesTo(SourceDirectorySet.class) Closure configureClosure);

    MinaSourceSet mina(Action<? super SourceDirectorySet> configureAction);

    SourceDirectorySet getAllMina();
}
