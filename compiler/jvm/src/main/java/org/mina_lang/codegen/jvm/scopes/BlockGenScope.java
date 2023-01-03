package org.mina_lang.codegen.jvm.scopes;

import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.map.MutableMap;
import org.mina_lang.common.Attributes;
import org.mina_lang.common.Meta;
import org.mina_lang.common.names.ConstructorName;
import org.mina_lang.syntax.BlockNode;
import org.objectweb.asm.Label;

public record BlockGenScope(
        JavaMethodScope enclosingMethod,
        Label startLabel,
        Label endLabel,
        MutableMap<String, Meta<Attributes>> values,
        MutableMap<String, Meta<Attributes>> types,
        MutableMap<ConstructorName, MutableMap<String, Meta<Attributes>>> fields) implements VarBindingScope {
    public BlockGenScope(JavaMethodScope enclosingMethod, Label startLabel, Label endLabel) {
        this(
                enclosingMethod,
                startLabel,
                endLabel,
                Maps.mutable.empty(),
                Maps.mutable.empty(),
                Maps.mutable.empty());
    }

    public static BlockGenScope open(JavaMethodScope enclosingMethod, BlockNode<Attributes> block) {
        var startLabel = new Label();
        var endLabel = new Label();

        enclosingMethod.methodWriter().visitLabel(startLabel);

        block.declarations().forEach(decl -> {
            enclosingMethod.putLocalVar(decl, startLabel, endLabel);
        });

        return new BlockGenScope(enclosingMethod, startLabel, endLabel);
    }

    public void finaliseBlock() {
        enclosingMethod
                .methodWriter()
                .visitLabel(endLabel);
    }
}
