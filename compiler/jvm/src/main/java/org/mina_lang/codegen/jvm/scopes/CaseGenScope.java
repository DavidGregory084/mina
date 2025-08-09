/*
 * SPDX-FileCopyrightText:  © 2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.codegen.jvm.scopes;

import org.mina_lang.codegen.jvm.LocalVar;
import org.mina_lang.common.Attributes;
import org.mina_lang.common.Meta;
import org.mina_lang.common.names.ConstructorName;
import org.mina_lang.common.names.Named;
import org.objectweb.asm.Label;
import org.objectweb.asm.commons.GeneratorAdapter;

import java.util.HashMap;
import java.util.Map;

public record CaseGenScope(
        JavaMethodScope enclosingMethod,
        MatchGenScope enclosingMatch,
        Label startLabel,
        Label endLabel,
        Map<String, Meta<Attributes>> values,
        Map<String, Meta<Attributes>> types,
        Map<ConstructorName, Map<String, Meta<Attributes>>> fields,
        Map<Named, LocalVar> localVars) implements VarBindingScope {
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
                new HashMap<>(),
                new HashMap<>(),
                new HashMap<>(),
                new HashMap<>());
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
