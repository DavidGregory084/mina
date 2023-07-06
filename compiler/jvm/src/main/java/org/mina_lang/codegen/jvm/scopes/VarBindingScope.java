/*
 * SPDX-FileCopyrightText:  © 2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.codegen.jvm.scopes;

import org.eclipse.collections.api.map.MutableMap;
import org.mina_lang.codegen.jvm.JavaSignature;
import org.mina_lang.codegen.jvm.LocalVar;
import org.mina_lang.codegen.jvm.Names;
import org.mina_lang.codegen.jvm.Types;
import org.mina_lang.common.Attributes;
import org.mina_lang.common.names.Named;
import org.mina_lang.syntax.MetaNode;
import org.objectweb.asm.Label;
import org.objectweb.asm.commons.GeneratorAdapter;

import java.util.Optional;

public interface VarBindingScope extends CodegenScope {
    GeneratorAdapter methodWriter();

    Label startLabel();

    Label endLabel();

    MutableMap<Named, LocalVar> localVars();

    default public boolean hasLocalVar(Named varName) {
        return localVars().containsKey(varName);
    }

    default public Optional<LocalVar> lookupLocalVar(Named varName) {
        return Optional.ofNullable(localVars().get(varName));
    }

    default public int putLocalVar(MetaNode<Attributes> localVar) {
        return putLocalVar(localVar, startLabel(), endLabel());
    }

    default public int putLocalVar(MetaNode<Attributes> localVar, Label startLabel, Label endLabel) {
        var varName = Names.getName(localVar);
        var varMinaType = Types.getType(localVar);
        var varType = Types.asmType(localVar);
        var varIndex = methodWriter().newLocal(varType);
        var varSignature = JavaSignature.forType(varMinaType);

        localVars().put(
                varName,
                new LocalVar(
                        0,
                        varIndex,
                        varName.localName(),
                        varType.getDescriptor(),
                        varSignature,
                        startLabel,
                        endLabel));

        return varIndex;
    }

    default public void visitLocalVars() {
        localVars().forEachKeyValue((name, localVar) -> {
            // We use the underlying MethodVisitor because inexplicably, the indices
            // provided by GeneratorAdapter's newLocal method are not compatible with its
            // own implementation of visitLocalVariable
            methodWriter().getDelegate().visitLocalVariable(
                    name.localName(),
                    localVar.descriptor(),
                    localVar.signature(),
                    localVar.startLabel(),
                    localVar.endLabel(),
                    localVar.index());
        });
    }
}
