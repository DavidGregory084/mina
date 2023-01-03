package org.mina_lang.codegen.jvm.scopes;

import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.map.MutableMap;
import org.mina_lang.common.Attributes;
import org.mina_lang.common.Meta;
import org.mina_lang.common.names.ConstructorName;
import org.objectweb.asm.Label;

public record CaseGenScope(
        JavaMethodScope enclosingMethod,
        MatchGenScope enclosingMatch,
        Label startLabel,
        Label endLabel,
        MutableMap<String, Meta<Attributes>> values,
        MutableMap<String, Meta<Attributes>> types,
        MutableMap<ConstructorName, MutableMap<String, Meta<Attributes>>> fields) implements VarBindingScope {
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
                Maps.mutable.empty());
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
    }
}
