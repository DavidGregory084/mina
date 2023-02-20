package org.mina_lang.gradle;

import static org.gradle.util.internal.ConfigureUtil.configure;
import static org.gradle.api.reflect.TypeOf.typeOf;

import org.gradle.api.Action;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.reflect.HasPublicType;
import org.gradle.api.reflect.TypeOf;

import groovy.lang.Closure;

public class DefaultMinaSourceSet implements MinaSourceSet, HasPublicType {
    private final MinaSourceDirectorySet mina;
    private final SourceDirectorySet allMina;

    public DefaultMinaSourceSet(String displayName, ObjectFactory objectFactory) {
        mina = objectFactory.newInstance(DefaultMinaSourceDirectorySet.class, objectFactory.sourceDirectorySet("mina", displayName + " Mina source"));
        mina.getFilter().include("**/*.mina");
        allMina = objectFactory.sourceDirectorySet("allmina", displayName + " Mina source");
        allMina.getFilter().include("**/*.mina");
        allMina.source(mina);
    }

    @Override
    public MinaSourceDirectorySet getMina() {
        return mina;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public MinaSourceSet mina(Closure configureClosure) {
        configure(configureClosure, getMina());
        return this;
    }

    @Override
    public MinaSourceSet mina(Action<? super SourceDirectorySet> configureAction) {
        configureAction.execute(getMina());
        return this;
    }

    @Override
    public SourceDirectorySet getAllMina() {
        return allMina;
    }

    @Override
    public TypeOf<?> getPublicType() {
        return typeOf(MinaSourceSet.class);
    }
}
