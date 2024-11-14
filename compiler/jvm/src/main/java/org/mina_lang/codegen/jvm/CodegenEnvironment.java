/*
 * SPDX-FileCopyrightText:  © 2023-2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.codegen.jvm;

import org.eclipse.collections.api.stack.MutableStack;
import org.eclipse.collections.impl.factory.Stacks;
import org.mina_lang.codegen.jvm.scopes.*;
import org.mina_lang.common.Attributes;
import org.mina_lang.common.Environment;
import org.mina_lang.common.names.Named;
import org.mina_lang.syntax.MetaNode;
import org.objectweb.asm.Label;
import org.objectweb.asm.commons.GeneratorAdapter;

import java.util.Optional;

public record CodegenEnvironment(MutableStack<CodegenScope> scopes) implements Environment<Attributes, CodegenScope> {

    public Optional<NamespaceGenScope> enclosingNamespace() {
        return scopes()
                .detectOptional(scope -> scope instanceof NamespaceGenScope)
                .map(scope -> (NamespaceGenScope) scope);
    }

    public Optional<DataGenScope> enclosingData() {
        return scopes()
                .detectOptional(scope -> scope instanceof DataGenScope)
                .map(scope -> (DataGenScope) scope);
    }

    public Optional<ConstructorGenScope> enclosingConstructor() {
        return scopes()
                .detectOptional(scope -> scope instanceof ConstructorGenScope)
                .map(scope -> (ConstructorGenScope) scope);
    }

    public Optional<LambdaGenScope> enclosingLambda() {
        return scopes()
                .detectOptional(scope -> scope instanceof LambdaGenScope)
                .map(scope -> (LambdaGenScope) scope);
    }

    public Optional<MatchGenScope> enclosingMatch() {
        return scopes()
                .detectOptional(scope -> scope instanceof MatchGenScope)
                .map(scope -> (MatchGenScope) scope);
    }

    public Optional<IfGenScope> enclosingIf() {
        return scopes()
                .detectOptional(scope -> scope instanceof IfGenScope)
                .map(scope -> (IfGenScope) scope);
    }

    public Optional<CaseGenScope> enclosingCase() {
        return scopes()
                .detectOptional(scope -> scope instanceof CaseGenScope)
                .map(scope -> (CaseGenScope) scope);
    }

    public Optional<BlockGenScope> enclosingBlock() {
        return scopes()
                .detectOptional(scope -> scope instanceof BlockGenScope)
                .map(scope -> (BlockGenScope) scope);
    }

    public Optional<TopLevelLetGenScope> enclosingTopLevelLet() {
        return scopes()
                .detectOptional(scope -> scope instanceof TopLevelLetGenScope)
                .map(scope -> (TopLevelLetGenScope) scope);
    }

    public Optional<LambdaLiftingScope> enclosingLambdaLifter() {
        return scopes()
                .detectOptional(scope -> scope instanceof LambdaLiftingScope)
                .map(scope -> (LambdaLiftingScope) scope);
    }

    public Optional<JavaMethodScope> enclosingJavaMethod() {
        return scopes()
                .detectOptional(scope -> scope instanceof JavaMethodScope)
                .map(scope -> (JavaMethodScope) scope);
    }

    public Optional<VarBindingScope> enclosingVarBinding() {
        return scopes()
                .detectOptional(scope -> scope instanceof VarBindingScope)
                .map(scope -> (VarBindingScope) scope);
    }

    public static CodegenEnvironment empty() {
        return new CodegenEnvironment(Stacks.mutable.empty());
    }

    public static CodegenEnvironment of(CodegenScope scope) {
        var scopes = Stacks.mutable.<CodegenScope>empty();
        scopes.push(scope);
        return new CodegenEnvironment(scopes);
    }

    public Optional<LocalVar> lookupLocalVarIn(GeneratorAdapter methodWriter, Named varName) {
        return scopes()
                .select(scope -> {
                    return scope instanceof VarBindingScope varBinder &&
                        varBinder.methodWriter().equals(methodWriter) && // Make sure we only look up local vars from the same Java method
                        varBinder.hasLocalVar(varName);
                })
                .getFirstOptional()
                .flatMap(varBinder -> ((VarBindingScope) varBinder).lookupLocalVar(varName));
    }

    public int putLocalVar(MetaNode<Attributes> localVar, Label startLabel, Label endLabel) {
        return enclosingVarBinding()
                .map(varBinder -> varBinder.putLocalVar(localVar, startLabel, endLabel))
                .get();
    }

    public int putLocalVar(MetaNode<Attributes> localVar) {
        return enclosingVarBinding()
                .map(varBinder -> varBinder.putLocalVar(localVar))
                .get();
    }
}
