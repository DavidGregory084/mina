package org.mina_lang.codegen.jvm.scopes;

import static org.objectweb.asm.Opcodes.ACC_FINAL;

import org.eclipse.collections.api.map.ImmutableMap;
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

public interface JavaMethodScope extends VarBindingScope {
    GeneratorAdapter methodWriter();

    ImmutableMap<Named, LocalVar> methodParams();

    MutableMap<Named, LocalVar> localVars();

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

    default public void visitMethodParams() {
        methodParams()
                .toSortedListBy(LocalVar::index)
                .forEach(param -> {
                    methodWriter().visitParameter(param.name(), ACC_FINAL);
                });
    }

    default public void visitLocalVars() {
        methodParams()
                .toSortedListBy(LocalVar::index)
                .forEach(param -> {
                    methodWriter().visitLocalVariable(
                            param.name(),
                            param.descriptor(),
                            param.signature(),
                            param.startLabel(),
                            param.endLabel(),
                            param.index());
                });
        localVars().forEachKeyValue((name, localVar) -> {
            methodWriter().visitLocalVariable(
                    name.localName(),
                    localVar.descriptor(),
                    localVar.signature(),
                    localVar.startLabel(),
                    localVar.endLabel(),
                    localVar.index());
        });
    }

    default void finaliseMethod() {
        methodWriter().returnValue();
        methodWriter().visitLabel(endLabel());
        visitMethodParams();
        visitLocalVars();
        methodWriter().visitEnd();
    }
}
