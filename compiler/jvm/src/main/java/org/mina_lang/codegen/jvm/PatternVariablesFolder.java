package org.mina_lang.codegen.jvm;

import java.util.Optional;

import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.set.ImmutableSet;
import org.mina_lang.common.Attributes;
import org.mina_lang.common.Meta;
import org.mina_lang.common.names.Name;
import org.mina_lang.syntax.PatternNodeFolder;
import org.mina_lang.syntax.QualifiedIdNode;

public class PatternVariablesFolder implements PatternNodeFolder<Attributes, ImmutableSet<Name>> {

    @Override
    public ImmutableSet<Name> visitBoolean(Meta<Attributes> meta, boolean value) {
        return Sets.immutable.empty();
    }

    @Override
    public ImmutableSet<Name> visitChar(Meta<Attributes> meta, char value) {
        return Sets.immutable.empty();
    }

    @Override
    public ImmutableSet<Name> visitString(Meta<Attributes> meta, String value) {
        return Sets.immutable.empty();
    }

    @Override
    public ImmutableSet<Name> visitInt(Meta<Attributes> meta, int value) {
        return Sets.immutable.empty();
    }

    @Override
    public ImmutableSet<Name> visitLong(Meta<Attributes> meta, long value) {
        return Sets.immutable.empty();
    }

    @Override
    public ImmutableSet<Name> visitFloat(Meta<Attributes> meta, float value) {
        return Sets.immutable.empty();
    }

    @Override
    public ImmutableSet<Name> visitDouble(Meta<Attributes> meta, double value) {
        return Sets.immutable.empty();
    }

    @Override
    public ImmutableSet<Name> visitAliasPattern(Meta<Attributes> meta, String alias, ImmutableSet<Name> pattern) {
        return pattern.newWith(meta.meta().name());
    }

    @Override
    public ImmutableSet<Name> visitConstructorPattern(Meta<Attributes> meta, QualifiedIdNode id,
            ImmutableList<ImmutableSet<Name>> fields) {
        return fields.flatCollect(fld -> fld).toImmutableSet();
    }

    @Override
    public ImmutableSet<Name> visitFieldPattern(Meta<Attributes> meta, String field,
            Optional<ImmutableSet<Name>> pattern) {
        return pattern.orElseGet(() -> Sets.immutable.of(meta.meta().name()));
    }

    @Override
    public ImmutableSet<Name> visitIdPattern(Meta<Attributes> meta, String name) {
        return Sets.immutable.of(meta.meta().name());
    }

    @Override
    public ImmutableSet<Name> visitLiteralPattern(Meta<Attributes> meta, ImmutableSet<Name> literal) {
        return Sets.immutable.empty();
    }
}
