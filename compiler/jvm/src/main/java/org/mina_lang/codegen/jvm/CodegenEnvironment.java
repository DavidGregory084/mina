/*
 * SPDX-FileCopyrightText:  © 2023-2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.codegen.jvm;

import org.mina_lang.codegen.jvm.scopes.*;
import org.mina_lang.common.Attributes;
import org.mina_lang.common.Environment;
import org.mina_lang.common.names.Named;
import org.mina_lang.syntax.MetaNode;
import org.objectweb.asm.Label;
import org.objectweb.asm.commons.GeneratorAdapter;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;

public record CodegenEnvironment(Deque<CodegenScope> scopes) implements Environment<Attributes, CodegenScope> {

    public Optional<NamespaceGenScope> enclosingNamespace() {
        return scopes().stream()
            .filter(scope -> scope instanceof NamespaceGenScope)
            .findFirst()
            .map(scope -> (NamespaceGenScope) scope);
    }

    public Optional<DataGenScope> enclosingData() {
        return scopes().stream()
            .filter(scope -> scope instanceof DataGenScope)
            .findFirst()
            .map(scope -> (DataGenScope) scope);
    }

    public Optional<ConstructorGenScope> enclosingConstructor() {
        return scopes().stream()
            .filter(scope -> scope instanceof ConstructorGenScope)
            .findFirst()
            .map(scope -> (ConstructorGenScope) scope);
    }

    public Optional<LambdaGenScope> enclosingLambda() {
        return scopes().stream()
                .filter(scope -> scope instanceof LambdaGenScope)
                .findFirst()
                .map(scope -> (LambdaGenScope) scope);
    }

    public Optional<MatchGenScope> enclosingMatch() {
        return scopes().stream()
                .filter(scope -> scope instanceof MatchGenScope)
            .findFirst()
                .map(scope -> (MatchGenScope) scope);
    }

    public Optional<IfGenScope> enclosingIf() {
        return scopes().stream()
                .filter(scope -> scope instanceof IfGenScope)
            .findFirst()
                .map(scope -> (IfGenScope) scope);
    }

    public Optional<CaseGenScope> enclosingCase() {
        return scopes().stream()
                .filter(scope -> scope instanceof CaseGenScope)
            .findFirst()
                .map(scope -> (CaseGenScope) scope);
    }

    public Optional<BlockGenScope> enclosingBlock() {
        return scopes().stream()
                .filter(scope -> scope instanceof BlockGenScope)
            .findFirst()
                .map(scope -> (BlockGenScope) scope);
    }

    public Optional<TopLevelLetGenScope> enclosingTopLevelLet() {
        return scopes().stream()
                .filter(scope -> scope instanceof TopLevelLetGenScope)
            .findFirst()
                .map(scope -> (TopLevelLetGenScope) scope);
    }

    public Optional<LambdaLiftingScope> enclosingLambdaLifter() {
        return scopes().stream()
                .filter(scope -> scope instanceof LambdaLiftingScope)
            .findFirst()
                .map(scope -> (LambdaLiftingScope) scope);
    }

    public Optional<JavaMethodScope> enclosingJavaMethod() {
        return scopes().stream()
                .filter(scope -> scope instanceof JavaMethodScope)
            .findFirst()
                .map(scope -> (JavaMethodScope) scope);
    }

    public Optional<VarBindingScope> enclosingVarBinding() {
        return scopes().stream()
                .filter(scope -> scope instanceof VarBindingScope)
            .findFirst()
                .map(scope -> (VarBindingScope) scope);
    }

    public static CodegenEnvironment empty() {
        return new CodegenEnvironment(new ArrayDeque<>());
    }

    public static CodegenEnvironment of(CodegenScope scope) {
        var scopes = new ArrayDeque<CodegenScope>();
        scopes.push(scope);
        return new CodegenEnvironment(scopes);
    }

    public Optional<LocalVar> lookupLocalVarIn(GeneratorAdapter methodWriter, Named varName) {
        return scopes().stream()
            .filter(scope -> {
                return scope instanceof VarBindingScope varBinder &&
                    varBinder.methodWriter().equals(methodWriter) && // Make sure we only look up local vars from the same Java method
                    varBinder.hasLocalVar(varName);
            })
            .findFirst()
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
