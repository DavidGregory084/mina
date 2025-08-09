/*
 * SPDX-FileCopyrightText:  © 2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.codegen.jvm;

import org.mina_lang.common.Attributes;
import org.mina_lang.common.Meta;
import org.mina_lang.common.names.Name;
import org.mina_lang.syntax.PatternNodeFolder;
import org.mina_lang.syntax.QualifiedIdNode;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


public class PatternVariablesFolder implements PatternNodeFolder<Attributes, Set<Name>> {

    @Override
    public Set<Name> visitBoolean(Meta<Attributes> meta, boolean value) {
        return new HashSet<>();
    }

    @Override
    public Set<Name> visitChar(Meta<Attributes> meta, char value) {
        return new HashSet<>();
    }

    @Override
    public Set<Name> visitString(Meta<Attributes> meta, String value) {
        return new HashSet<>();
    }

    @Override
    public Set<Name> visitInt(Meta<Attributes> meta, int value) {
        return new HashSet<>();
    }

    @Override
    public Set<Name> visitLong(Meta<Attributes> meta, long value) {
        return new HashSet<>();
    }

    @Override
    public Set<Name> visitFloat(Meta<Attributes> meta, float value) {
        return new HashSet<>();
    }

    @Override
    public Set<Name> visitDouble(Meta<Attributes> meta, double value) {
        return new HashSet<>();
    }

    @Override
    public Set<Name> visitAliasPattern(Meta<Attributes> meta, String alias, Set<Name> pattern) {
        var aliasPattern = new HashSet<>(pattern);
        aliasPattern.add(meta.meta().name());
        return aliasPattern;
    }

    @Override
    public Set<Name> visitConstructorPattern(Meta<Attributes> meta, QualifiedIdNode id,
            List<Set<Name>> fields) {
        return fields.stream().flatMap(Set::stream).collect(Collectors.toSet());
    }

    @Override
    public Set<Name> visitFieldPattern(Meta<Attributes> meta, String field, Set<Name> pattern) {
        return pattern;
    }

    @Override
    public Set<Name> visitIdPattern(Meta<Attributes> meta, String name) {
        return Set.of(meta.meta().name());
    }

    @Override
    public Set<Name> visitLiteralPattern(Meta<Attributes> meta, Set<Name> literal) {
        return new HashSet<>();
    }
}
