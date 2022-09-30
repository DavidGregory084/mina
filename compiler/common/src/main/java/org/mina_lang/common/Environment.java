package org.mina_lang.common;

import java.util.Optional;
import java.util.function.BiConsumer;

import org.eclipse.collections.api.block.function.Function3;
import org.eclipse.collections.api.factory.Stacks;
import org.eclipse.collections.api.stack.MutableStack;

/**
 * Represents a name or type environment as a stack of scopes,
 * which are searched innermost to outermost when resolving names.
 * <p>
 * Inspired by the "scopes" and "ribs" of the Rust compiler naming phase as
 * described in the
 * <a href="https://rustc-dev-guide.rust-lang.org/name-resolution.html">Guide To
 * Rustc Development</a>.
 */
public record Environment<A> (MutableStack<Scope<A>> scopes) {

    public Scope<A> topScope() {
        return scopes.peek();
    }

    public Optional<NamespaceScope<A>> enclosingNamespace() {
        return scopes
                .detectOptional(scope -> scope instanceof NamespaceScope)
                .map(scope -> (NamespaceScope<A>) scope);
    }

    public Optional<DataScope<A>> enclosingData() {
        return scopes
                .detectOptional(scope -> scope instanceof DataScope)
                .map(scope -> (DataScope<A>) scope);
    }

    public Optional<ConstructorScope<A>> enclosingConstructor() {
        return scopes
                .detectOptional(scope -> scope instanceof ConstructorScope)
                .map(scope -> (ConstructorScope<A>) scope);
    }

    public Optional<LambdaScope<A>> enclosingLambda() {
        return scopes
                .detectOptional(scope -> scope instanceof LambdaScope)
                .map(scope -> (LambdaScope<A>) scope);
    }

    public Optional<CaseScope<A>> enclosingCase() {
        return scopes
                .detectOptional(scope -> scope instanceof CaseScope)
                .map(scope -> (CaseScope<A>) scope);
    }

    public Optional<ConstructorPatternScope<A>> enclosingConstructorPattern() {
        return scopes
                .detectOptional(scope -> scope instanceof ConstructorPatternScope)
                .map(scope -> (ConstructorPatternScope<A>) scope);
    }

    public Optional<BlockScope<A>> enclosingBlock() {
        return scopes
                .detectOptional(scope -> scope instanceof BlockScope)
                .map(scope -> (BlockScope<A>) scope);
    }

    public Optional<Meta<A>> lookupValue(String name) {
        return scopes
                .detectOptional(scope -> scope.hasValue(name))
                .flatMap(scope -> scope.lookupValue(name));
    };

    public <B> Optional<Meta<A>> lookupValueOrElse(String name, Meta<B> meta, BiConsumer<String, Meta<B>> orElseFn) {
        var valueMeta = lookupValue(name);

        if (valueMeta.isEmpty()) {
            orElseFn.accept(name, meta);
        }

        return valueMeta;
    };

    public boolean populateValue(String name, Meta<A> meta) {
        return lookupValue(name)
                .map(scope -> false) // Duplicate definition
                .orElseGet(() -> {
                    return scopes.getFirstOptional()
                            .map(scope -> scope.populateValue(name, meta))
                            .orElse(false);
                });
    }

    public void populateValueOrElse(String name, Meta<A> proposed,
            Function3<String, Meta<A>, Meta<A>, Void> orElseFn) {
        if (!populateValue(name, proposed)) {
            scopes.getFirstOptional()
                    .ifPresent(scope -> scope.populateValueOrElse(name, proposed, orElseFn));
        }
    }

    public Optional<Meta<A>> lookupType(String name) {
        return scopes
                .detectOptional(scope -> scope.hasType(name))
                .flatMap(scope -> scope.lookupType(name));
    };

    public <B> Optional<Meta<A>> lookupTypeOrElse(String name, Meta<B> meta, BiConsumer<String, Meta<B>> orElseFn) {
        var typeMeta = lookupType(name);

        if (typeMeta.isEmpty()) {
            orElseFn.accept(name, meta);
        }

        return typeMeta;
    };

    public boolean populateType(String name, Meta<A> meta) {
        return lookupType(name)
                .map(scope -> false) // Duplicate definition
                .orElseGet(() -> {
                    return scopes.getFirstOptional()
                            .map(scope -> scope.populateType(name, meta))
                            .orElse(false);
                });
    }

    public void populateTypeOrElse(String name, Meta<A> proposed,
            Function3<String, Meta<A>, Meta<A>, Void> orElseFn) {
        if (!populateType(name, proposed)) {
            scopes.getFirstOptional()
                    .ifPresent(scope -> scope.populateTypeOrElse(name, proposed, orElseFn));
        }
    }

    public Optional<Meta<A>> lookupField(ConstructorName constr, String name) {
        return scopes
                .detectOptional(scope -> scope.hasField(constr, name))
                .flatMap(scope -> scope.lookupField(constr, name));
    };

    public <B> Optional<Meta<A>> lookupFieldOrElse(ConstructorName constr, String name, Meta<B> meta,
            BiConsumer<String, Meta<B>> orElseFn) {
        var fieldMeta = lookupField(constr, name);

        if (fieldMeta.isEmpty()) {
            orElseFn.accept(name, meta);
        }

        return fieldMeta;
    };

    public boolean populateField(ConstructorName constr, String name, Meta<A> meta) {
        return lookupField(constr, name)
                .map(scope -> false) // Duplicate definition
                .orElseGet(() -> {
                    return scopes.getFirstOptional()
                            .map(scope -> scope.populateField(constr, name, meta))
                            .orElse(false);
                });
    }

    public void populateFieldOrElse(ConstructorName constr, String name, Meta<A> proposed,
            Function3<String, Meta<A>, Meta<A>, Void> orElseFn) {
        if (!populateField(constr, name, proposed)) {
            scopes.getFirstOptional()
                    .ifPresent(scope -> scope.populateFieldOrElse(constr, name, proposed, orElseFn));
        }
    }

    public void pushScope(Scope<A> scope) {
        scopes.push(scope);
    }

    public void popScope(Class<?> expected) {
        var poppedScope = scopes.pop();
        assert expected.isAssignableFrom(poppedScope.getClass());
    }

    public static <A> Environment<A> empty() {
        return new Environment<>(Stacks.mutable.empty());
    }

    public static <A> Environment<A> of(ImportedScope<A> scope) {
        var scopeStack = Stacks.mutable.<Scope<A>>empty();
        scopeStack.push(scope);
        return new Environment<>(scopeStack);
    }
}