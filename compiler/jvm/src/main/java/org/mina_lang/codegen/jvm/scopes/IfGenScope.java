/*
 * SPDX-FileCopyrightText:  © 2022-2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.codegen.jvm.scopes;

import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.impl.factory.Maps;
import org.mina_lang.common.Attributes;
import org.mina_lang.common.Meta;
import org.mina_lang.common.names.ConstructorName;
import org.objectweb.asm.Label;

public record IfGenScope(
        Label thenLabel,
        Label elseLabel,
        Label endLabel,
        MutableMap<String, Meta<Attributes>> values,
        MutableMap<String, Meta<Attributes>> types,
        MutableMap<ConstructorName, MutableMap<String, Meta<Attributes>>> fields) implements CodegenScope {
    public IfGenScope(Label thenLabel, Label elseLabel, Label endLabel) {
        this(
            thenLabel,
            elseLabel,
            endLabel,
            Maps.mutable.empty(),
            Maps.mutable.empty(),
            Maps.mutable.empty());
    }

    public static IfGenScope open() {
        var thenLabel = new Label();
        var elseLabel = new Label();
        var endLabel = new Label();
        return new IfGenScope(thenLabel, elseLabel, endLabel);
    }
}
