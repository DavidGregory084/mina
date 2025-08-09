/*
 * SPDX-FileCopyrightText:  © 2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.codegen.jvm.scopes;

import org.mina_lang.common.Attributes;
import org.mina_lang.common.Meta;
import org.mina_lang.common.names.ConstructorName;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;

import java.util.HashMap;
import java.util.Map;

public record MatchGenScope(
        JavaMethodScope enclosingMethod,
        Label startLabel,
        Label endLabel,
        Map<String, Meta<Attributes>> values,
        Map<String, Meta<Attributes>> types,
        Map<ConstructorName, Map<String, Meta<Attributes>>> fields) implements CodegenScope {

    public MatchGenScope(JavaMethodScope enclosingMethod, Label startLabel, Label endLabel) {
        this(
                enclosingMethod,
                startLabel,
                endLabel,
                new HashMap<>(),
                new HashMap<>(),
                new HashMap<>());
    }

    public static MatchGenScope open(JavaMethodScope enclosingMethod) {
        var startLabel = new Label();
        var endLabel = new Label();
        enclosingMethod.methodWriter().visitLabel(startLabel);
        return new MatchGenScope(enclosingMethod, startLabel, endLabel);
    }

    public void finaliseMatch() {
        // TODO: Throw java.lang.MatchException once it leaves preview
        enclosingMethod
            .methodWriter()
            .throwException(Type.getType(RuntimeException.class), "Match error");
        enclosingMethod
                .methodWriter()
                .visitLabel(endLabel);
    }
}
