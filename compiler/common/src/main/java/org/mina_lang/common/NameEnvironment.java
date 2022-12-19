package org.mina_lang.common;

import java.util.Optional;

import org.eclipse.collections.api.factory.Stacks;
import org.eclipse.collections.api.stack.MutableStack;
import org.mina_lang.common.names.Name;
import org.mina_lang.common.scopes.NamingScope;
import org.mina_lang.common.scopes.naming.*;

public record NameEnvironment(MutableStack<NamingScope> scopes) implements Environment<Name, NamingScope> {

    public Optional<NamespaceNamingScope> enclosingNamespace() {
        return scopes()
                .detectOptional(scope -> scope instanceof NamespaceNamingScope)
                .map(scope -> (NamespaceNamingScope) scope);
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
