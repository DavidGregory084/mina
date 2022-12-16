package org.mina_lang.common;

import org.eclipse.collections.api.factory.Stacks;
import org.eclipse.collections.api.stack.MutableStack;
import org.mina_lang.common.names.Name;
import org.mina_lang.common.scopes.BuiltInScope;
import org.mina_lang.common.scopes.Scope;

public record NameEnvironment(MutableStack<Scope<Name>> scopes) implements Environment<Name> {

    public static NameEnvironment empty() {
        return new NameEnvironment(Stacks.mutable.empty());
    }

    public static NameEnvironment of(Scope<Name> scope) {
        var scopes = Stacks.mutable.<Scope<Name>>empty();
        scopes.push(scope);
        return new NameEnvironment(scopes);
    }

    public static NameEnvironment withBuiltInNames() {
        return NameEnvironment.of(BuiltInScope.withBuiltInNames());
    }
}
