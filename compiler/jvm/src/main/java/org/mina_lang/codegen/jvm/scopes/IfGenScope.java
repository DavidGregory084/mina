/*
 * SPDX-FileCopyrightText:  © 2022-2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.codegen.jvm.scopes;

import org.mina_lang.common.Attributes;
import org.mina_lang.common.Meta;
import org.mina_lang.common.names.ConstructorName;
import org.objectweb.asm.Label;

import java.util.HashMap;
import java.util.Map;

public record IfGenScope(
        Label thenLabel,
        Label elseLabel,
        Label endLabel,
        Map<String, Meta<Attributes>> values,
        Map<String, Meta<Attributes>> types,
        Map<ConstructorName, Map<String, Meta<Attributes>>> fields) implements CodegenScope {
    public IfGenScope(Label thenLabel, Label elseLabel, Label endLabel) {
        this(
            thenLabel,
            elseLabel,
            endLabel,
            new HashMap<>(),
            new HashMap<>(),
            new HashMap<>());
    }

    public static IfGenScope open() {
        var thenLabel = new Label();
        var elseLabel = new Label();
        var endLabel = new Label();
        return new IfGenScope(thenLabel, elseLabel, endLabel);
    }
}
