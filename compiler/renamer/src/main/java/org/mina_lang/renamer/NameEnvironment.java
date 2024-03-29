/*
 * SPDX-FileCopyrightText:  © 2022-2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.renamer;

import org.eclipse.collections.api.stack.MutableStack;
import org.eclipse.collections.impl.factory.Stacks;
import org.mina_lang.common.Environment;
import org.mina_lang.common.names.Name;
import org.mina_lang.renamer.scopes.*;

import java.util.Optional;

public record NameEnvironment(MutableStack<NamingScope> scopes) implements Environment<Name, NamingScope> {

    public Optional<NamespaceNamingScope> enclosingNamespace() {
        return scopes()
                .detectOptional(scope -> scope instanceof NamespaceNamingScope)
                .map(scope -> (NamespaceNamingScope) scope);
    }

    public Optional<DeclarationNamingScope> enclosingDeclaration() {
        return scopes()
                .detectOptional(scope -> scope instanceof DeclarationNamingScope)
                .map(scope -> (DeclarationNamingScope) scope);
    }

    public Optional<DataNamingScope> enclosingData() {
        return scopes()
                .detectOptional(scope -> scope instanceof DataNamingScope)
                .map(scope -> (DataNamingScope) scope);
    }

    public Optional<ConstructorNamingScope> enclosingConstructor() {
        return scopes()
                .detectOptional(scope -> scope instanceof ConstructorNamingScope)
                .map(scope -> (ConstructorNamingScope) scope);
    }

    public Optional<LetNamingScope> enclosingLet() {
        return scopes()
                .detectOptional(scope -> scope instanceof LetNamingScope)
                .map(scope -> (LetNamingScope) scope);
    }

    public Optional<LambdaNamingScope> enclosingLambda() {
        return scopes()
                .detectOptional(scope -> scope instanceof LambdaNamingScope)
                .map(scope -> (LambdaNamingScope) scope);
    }

    public Optional<CaseNamingScope> enclosingCase() {
        return scopes()
                .detectOptional(scope -> scope instanceof CaseNamingScope)
                .map(scope -> (CaseNamingScope) scope);
    }

    public Optional<ConstructorPatternNamingScope> enclosingConstructorPattern() {
        return scopes()
                .detectOptional(scope -> scope instanceof ConstructorPatternNamingScope)
                .map(scope -> (ConstructorPatternNamingScope) scope);
    }

    public Optional<BlockNamingScope> enclosingBlock() {
        return scopes()
                .detectOptional(scope -> scope instanceof BlockNamingScope)
                .map(scope -> (BlockNamingScope) scope);
    }

    public static NameEnvironment empty() {
        return new NameEnvironment(Stacks.mutable.empty());
    }

    public static NameEnvironment of(NamingScope scope) {
        var scopes = Stacks.mutable.<NamingScope>empty();
        scopes.push(scope);
        return new NameEnvironment(scopes);
    }

    public static NameEnvironment withBuiltInNames() {
        return NameEnvironment.of(BuiltInNamingScope.empty());
    }
}
