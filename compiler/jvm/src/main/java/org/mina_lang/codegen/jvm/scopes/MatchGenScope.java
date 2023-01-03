package org.mina_lang.codegen.jvm.scopes;

import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.map.MutableMap;
import org.mina_lang.common.Attributes;
import org.mina_lang.common.Meta;
import org.mina_lang.common.names.ConstructorName;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;

public record MatchGenScope(
        JavaMethodScope enclosingMethod,
        Label startLabel,
        Label endLabel,
        MutableMap<String, Meta<Attributes>> values,
        MutableMap<String, Meta<Attributes>> types,
        MutableMap<ConstructorName, MutableMap<String, Meta<Attributes>>> fields) implements VarBindingScope {

    public MatchGenScope(JavaMethodScope enclosingMethod, Label startLabel, Label endLabel) {
        this(
                enclosingMethod,
                startLabel,
                endLabel,
                Maps.mutable.empty(),
                Maps.mutable.empty(),
                Maps.mutable.empty());
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
