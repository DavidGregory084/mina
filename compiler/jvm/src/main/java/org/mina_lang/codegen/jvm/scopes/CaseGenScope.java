/*
 * SPDX-FileCopyrightText:  © 2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.codegen.jvm.scopes;

import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.impl.factory.Maps;
import org.mina_lang.codegen.jvm.LocalVar;
import org.mina_lang.common.Attributes;
import org.mina_lang.common.Meta;
import org.mina_lang.common.names.ConstructorName;
import org.mina_lang.common.names.Named;
import org.objectweb.asm.Label;
import org.objectweb.asm.commons.GeneratorAdapter;

public record CaseGenScope(
        JavaMethodScope enclosingMethod,
        MatchGenScope enclosingMatch,
        Label startLabel,
        Label endLabel,
        MutableMap<String, Meta<Attributes>> values,
        MutableMap<String, Meta<Attributes>> types,
        MutableMap<ConstructorName, MutableMap<String, Meta<Attributes>>> fields,
        MutableMap<Named, LocalVar> localVars) implements VarBindingScope {
    public CaseGenScope(
        JavaMethodScope enclosingMethod,
        MatchGenScope enclosingMatch,
        Label startLabel,
        Label endLabel) {
        this(
                enclosingMethod,
                enclosingMatch,
                startLabel,
                endLabel,
                Maps.mutable.empty(),
                Maps.mutable.empty(),
                Maps.mutable.empty(),
                Maps.mutable.empty());
    }

    @Override
    public GeneratorAdapter methodWriter() {
        return enclosingMethod.methodWriter();
    }

    public static CaseGenScope open(
        JavaMethodScope enclosingMethod,
        MatchGenScope enclosingMatch) {
        var startLabel = new Label();
        var endLabel = new Label();

        return new CaseGenScope(enclosingMethod, enclosingMatch, startLabel, endLabel);
    }

    public void finaliseCase() {
        enclosingMethod
            .methodWriter()
            .goTo(enclosingMatch.endLabel());
        enclosingMethod
                .methodWriter()
                .visitLabel(endLabel);
        visitLocalVars();
    }
}
