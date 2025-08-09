/*
 * SPDX-FileCopyrightText:  © 2022-2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.renamer;

import org.mina_lang.common.Environment;
import org.mina_lang.common.names.Name;
import org.mina_lang.renamer.scopes.*;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;

public record NameEnvironment(Deque<NamingScope> scopes) implements Environment<Name, NamingScope> {

    public Optional<NamespaceNamingScope> enclosingNamespace() {
        return scopes().stream()
            .filter(scope -> scope instanceof NamespaceNamingScope)
            .findFirst()
            .map(scope -> (NamespaceNamingScope) scope);
    }

    public Optional<DeclarationNamingScope> enclosingDeclaration() {
        return scopes().stream()
            .filter(scope -> scope instanceof DeclarationNamingScope)
            .findFirst()
            .map(scope -> (DeclarationNamingScope) scope);
    }

    public Optional<DataNamingScope> enclosingData() {
        return scopes().stream()
            .filter(scope -> scope instanceof DataNamingScope)
            .findFirst()
            .map(scope -> (DataNamingScope) scope);
    }

    public Optional<ConstructorNamingScope> enclosingConstructor() {
        return scopes().stream()
            .filter(scope -> scope instanceof ConstructorNamingScope)
            .findFirst()
            .map(scope -> (ConstructorNamingScope) scope);
    }

    public Optional<LetNamingScope> enclosingLet() {
        return scopes().stream()
            .filter(scope -> scope instanceof LetNamingScope)
            .findFirst()
            .map(scope -> (LetNamingScope) scope);
    }

    public Optional<LambdaNamingScope> enclosingLambda() {
        return scopes().stream()
            .filter(scope -> scope instanceof LambdaNamingScope)
            .findFirst()
            .map(scope -> (LambdaNamingScope) scope);
    }

    public Optional<CaseNamingScope> enclosingCase() {
        return scopes().stream()
            .filter(scope -> scope instanceof CaseNamingScope)
            .findFirst()
            .map(scope -> (CaseNamingScope) scope);
    }

    public Optional<ConstructorPatternNamingScope> enclosingConstructorPattern() {
        return scopes().stream()
            .filter(scope -> scope instanceof ConstructorPatternNamingScope)
            .findFirst()
            .map(scope -> (ConstructorPatternNamingScope) scope);
    }

    public Optional<BlockNamingScope> enclosingBlock() {
        return scopes().stream()
            .filter(scope -> scope instanceof BlockNamingScope)
            .findFirst()
            .map(scope -> (BlockNamingScope) scope);
    }

    public static NameEnvironment empty() {
        return new NameEnvironment(new ArrayDeque<>());
    }

    public static NameEnvironment of(NamingScope scope) {
        var scopes = new ArrayDeque<NamingScope>();
        scopes.push(scope);
        return new NameEnvironment(scopes);
    }

    public static NameEnvironment withBuiltInNames() {
        return NameEnvironment.of(BuiltInNamingScope.empty());
    }
}
