package org.mina_lang.common;

import java.util.Optional;
import java.util.function.BiConsumer;

import org.eclipse.collections.api.block.function.Function3;
import org.eclipse.collections.api.stack.MutableStack;
import org.mina_lang.common.names.ConstructorName;
import org.mina_lang.common.scopes.Scope;

/**
 * Represents a name or type environment as a stack of scopes,
 * which are searched innermost to outermost when resolving names.
 * <p>
 * Inspired by the "scopes" and "ribs" of the Rust compiler naming phase as
 * described in the
 * <a href="https://rustc-dev-guide.rust-lang.org/name-resolution.html">Guide To
 * Rustc Development</a>.
 */
public interface Environment<A, B extends Scope<A>> {

    MutableStack<B> scopes();

    default public B topScope() {
        return scopes().peek();
    }

    default public Optional<Meta<A>> lookupValue(String name) {
        return scopes()
                .detectOptional(scope -> scope.hasValue(name))
                .flatMap(scope -> scope.lookupValue(name));
    };

    default public <C> Optional<Meta<A>> lookupValueOrElse(String name, Meta<C> meta,
            BiConsumer<String, Meta<C>> orElseFn) {
        var valueMeta = lookupValue(name);

        if (valueMeta.isEmpty()) {
            orElseFn.accept(name, meta);
        }

        return valueMeta;
    };

    default void putValue(String name, Meta<A> meta) {
        scopes()
                .getFirstOptional()
                .ifPresent(scope -> scope.putValue(name, meta));
    }

    default public Meta<A> putValueIfAbsent(String name, Meta<A> meta) {
        return lookupValue(name)
                .orElseGet(() -> {
                    return scopes()
                            .getFirstOptional()
                            .map(scope -> scope.putValueIfAbsent(name, meta))
                            .orElse(null);
                });
    }

    default public void putValueIfAbsentOrElse(String name, Meta<A> proposed,
            Function3<String, Meta<A>, Meta<A>, Void> orElseFn) {
        var existing = putValueIfAbsent(name, proposed);
        if (existing != null) {
            orElseFn.value(name, proposed, existing);
        }
    }

    default public Optional<Meta<A>> lookupType(String name) {
        return scopes()
                .detectOptional(scope -> scope.hasType(name))
                .flatMap(scope -> scope.lookupType(name));
    };

    default public <C> Optional<Meta<A>> lookupTypeOrElse(String name, Meta<C> meta,
            BiConsumer<String, Meta<C>> orElseFn) {
        var typeMeta = lookupType(name);

        if (typeMeta.isEmpty()) {
            orElseFn.accept(name, meta);
        }

        return typeMeta;
    };

    default void putType(String name, Meta<A> meta) {
        scopes()
                .getFirstOptional()
                .ifPresent(scope -> scope.putType(name, meta));
    }

    default public Meta<A> putTypeIfAbsent(String name, Meta<A> meta) {
        return lookupType(name)
                .orElseGet(() -> {
                    return scopes()
                            .getFirstOptional()
                            .map(scope -> scope.putTypeIfAbsent(name, meta))
                            .orElse(null);
                });
    }

    default public void putTypeIfAbsentOrElse(String name, Meta<A> proposed,
            Function3<String, Meta<A>, Meta<A>, Void> orElseFn) {
        var existing = putTypeIfAbsent(name, proposed);
        if (existing != null) {
            orElseFn.value(name, proposed, existing);
        }
    }

    default public Optional<Meta<A>> lookupField(ConstructorName constr, String name) {
        return scopes()
                .detectOptional(scope -> scope.hasField(constr, name))
                .flatMap(scope -> scope.lookupField(constr, name));
    };

    default public <C> Optional<Meta<A>> lookupFieldOrElse(ConstructorName constr, String name, Meta<C> meta,
            BiConsumer<String, Meta<C>> orElseFn) {
        var fieldMeta = lookupField(constr, name);

        if (fieldMeta.isEmpty()) {
            orElseFn.accept(name, meta);
        }

        return fieldMeta;
    };

    default void putField(ConstructorName constr, String name, Meta<A> meta) {
        scopes()
                .getFirstOptional()
                .ifPresent(scope -> scope.putField(constr, name, meta));
    }

    default public Meta<A> putFieldIfAbsent(ConstructorName constr, String name, Meta<A> meta) {
        return lookupField(constr, name)
                .orElseGet(() -> {
                    return scopes()
                            .getFirstOptional()
                            .map(scope -> scope.putFieldIfAbsent(constr, name, meta))
                            .orElse(null);
                });
    }

    default public void putFieldIfAbsentOrElse(ConstructorName constr, String name, Meta<A> proposed,
            Function3<String, Meta<A>, Meta<A>, Void> orElseFn) {
        var existing = putFieldIfAbsent(constr, name, proposed);
        if (existing != null) {
            orElseFn.value(name, proposed, existing);
        }
    }

    default public void pushScope(B scope) {
        scopes().push(scope);
    }

    default public void popScope(Class<?> expected) {
        var poppedScope = scopes().pop();
        assert expected.isAssignableFrom(poppedScope.getClass());
    }
}
