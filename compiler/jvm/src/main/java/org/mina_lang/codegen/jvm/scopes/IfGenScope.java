package org.mina_lang.codegen.jvm.scopes;

import org.eclipse.collections.impl.factory.Maps;
import org.eclipse.collections.api.map.MutableMap;
import org.mina_lang.common.Attributes;
import org.mina_lang.common.Meta;
import org.mina_lang.common.names.ConstructorName;
import org.objectweb.asm.Label;

public record IfGenScope(
        Label elseLabel,
        Label endLabel,
        MutableMap<String, Meta<Attributes>> values,
        MutableMap<String, Meta<Attributes>> types,
        MutableMap<ConstructorName, MutableMap<String, Meta<Attributes>>> fields) implements CodegenScope {
    public IfGenScope(Label elseLabel, Label endLabel) {
        this(
                elseLabel,
                endLabel,
                Maps.mutable.empty(),
                Maps.mutable.empty(),
                Maps.mutable.empty());
    }

    public static IfGenScope open() {
        var elseLabel = new Label();
        var endLabel = new Label();
        return new IfGenScope(elseLabel, endLabel);
    }
}
