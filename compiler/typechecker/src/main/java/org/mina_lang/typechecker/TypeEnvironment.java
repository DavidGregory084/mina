/*
 * SPDX-FileCopyrightText:  © 2022-2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.typechecker;

import org.eclipse.collections.api.stack.MutableStack;
import org.eclipse.collections.impl.factory.Stacks;
import org.mina_lang.common.Attributes;
import org.mina_lang.common.Environment;
import org.mina_lang.common.names.Named;
import org.mina_lang.common.types.*;
import org.mina_lang.typechecker.scopes.*;

import java.util.Optional;

public record TypeEnvironment(
        MutableStack<TypingScope> scopes,
        UnionFind<MonoType> typeSubstitution,
        UnionFind<Kind> kindSubstitution,
        SortSubstitutionTransformer sortTransformer) implements Environment<Attributes, TypingScope> {

    @Override
    public void popScope(Class<?> expected) {
        var poppedScope = scopes().pop();

        if (poppedScope instanceof TypeVariableScope) {
            // Ensure that we apply any solutions to complex types in the substitution before we drop this scope
            typeSubstitution.updateWith((type, solution) -> {
                return solution.equals(type)
                    ? solution
                    : solution.accept(sortTransformer.getTypeTransformer());
            });
            poppedScope.unsolvedTypes().forEach(typeSubstitution::remove);
            poppedScope.syntheticVars().forEach(typeSubstitution::remove);
        } else if (poppedScope instanceof KindVariableScope) {
            kindSubstitution.updateWith((kind, solution) -> {
                return solution.equals(kind)
                    ? solution
                    : solution.accept(sortTransformer.getKindTransformer());
            });
            poppedScope.unsolvedKinds().forEach(kindSubstitution::remove);
        }

        assert expected.isAssignableFrom(poppedScope.getClass());
    }

    public Optional<NamespaceTypingScope> enclosingNamespace() {
        return scopes()
                .detectOptional(scope -> scope instanceof NamespaceTypingScope)
                .map(scope -> (NamespaceTypingScope) scope);
    }

    public Optional<DataTypingScope> enclosingData() {
        return scopes()
                .detectOptional(scope -> scope instanceof DataTypingScope)
                .map(scope -> (DataTypingScope) scope);
    }

    public Optional<ConstructorTypingScope> enclosingConstructor() {
        return scopes()
                .detectOptional(scope -> scope instanceof ConstructorTypingScope)
                .map(scope -> (ConstructorTypingScope) scope);
    }

    public Optional<LambdaTypingScope> enclosingLambda() {
        return scopes()
                .detectOptional(scope -> scope instanceof LambdaTypingScope)
                .map(scope -> (LambdaTypingScope) scope);
    }

    public Optional<CaseTypingScope> enclosingCase() {
        return scopes()
                .detectOptional(scope -> scope instanceof CaseTypingScope)
                .map(scope -> (CaseTypingScope) scope);
    }

    public Optional<BlockTypingScope> enclosingBlock() {
        return scopes()
                .detectOptional(scope -> scope instanceof BlockTypingScope)
                .map(scope -> (BlockTypingScope) scope);
    }

    public void putUnsolvedKind(UnsolvedKind unsolved) {
        kindSubstitution().add(unsolved);
        topScope().unsolvedKinds().add(unsolved);
    }

    public void putUnsolvedType(UnsolvedType unsolved) {
        typeSubstitution().add(unsolved);
        topScope().unsolvedTypes().add(unsolved);
    }

    public void putSyntheticVar(SyntheticVar synthetic) {
        typeSubstitution().add(synthetic);
        topScope().syntheticVars().add(synthetic);
    }

    public void solveType(UnsolvedType unsolved, MonoType solution) {
        typeSubstitution().union(unsolved, solution);
    }

    public void solveType(Named name, MonoType solution) {
        var existingMeta = lookupValue(name.canonicalName()).get();
        var existingType = (Type) existingMeta.meta().sort();
        var existingKind = existingType.kind();
        if (existingKind instanceof UnsolvedKind unsolvedKind) {
            solveKind(unsolvedKind, solution.kind());
        }
        if (existingType instanceof UnsolvedType unsolvedType) {
            solveType(unsolvedType, solution);
        }
    }

    public void solveKind(UnsolvedKind unsolved, Kind solution) {
        kindSubstitution().union(unsolved, solution);
    }

    public void solveKind(Named name, Kind solution) {
        var existingMeta = lookupType(name.canonicalName()).get();
        var existingKind = (Kind) existingMeta.meta().sort();
        if (existingKind instanceof UnsolvedKind unsolvedKind) {
            solveKind(unsolvedKind, solution);
        }
    }

    public static Kind pickKindConstant(MutableStack<TypingScope> scopes, Kind left, Kind right) {
        if (left instanceof UnsolvedKind unsolvedLeft) {
            if (right instanceof UnsolvedKind unsolvedRight) {
                var leftDepth = -1;
                var rightDepth = -1;
                // TODO: use MutableStack#indexWhere once it's implemented
                for (var i = 0; i < scopes.size(); i++) {
                    if (scopes.peekAt(i).unsolvedKinds().contains(unsolvedLeft)) {
                        leftDepth = i;
                    }
                    if (scopes.peekAt(i).unsolvedKinds().contains(unsolvedRight)) {
                        rightDepth = i;
                    }
                }
                // Pick kind variables defined in outer scopes
                return leftDepth > rightDepth
                    ? left
                    // Pick kind variables with lower id
                    : unsolvedLeft.id() < unsolvedRight.id()
                    ? left : right ;
            } else {
                return right;
            }
        } else {
            return left;
        }
    };

    public static MonoType pickTypeConstant(MutableStack<TypingScope> scopes, MonoType left, MonoType right) {
        if (left instanceof UnsolvedType unsolvedLeft) {
            if (right instanceof UnsolvedType unsolvedRight) {
                var leftDepth = -1;
                var rightDepth = -1;
                // TODO: use MutableStack#indexWhere once it's implemented
                for (var i = 0; i < scopes.size(); i++) {
                    if (scopes.peekAt(i).unsolvedTypes().contains(unsolvedLeft)) {
                        leftDepth = i;
                    }
                    if (scopes.peekAt(i).unsolvedTypes().contains(unsolvedRight)) {
                        rightDepth = i;
                    }
                }
                // Pick type variables defined in outer scopes
                return leftDepth > rightDepth
                    ? left
                    // Pick type variables with lower id
                    : unsolvedLeft.id() < unsolvedRight.id()
                    ? left : right ;
            } else {
                return right;
            }
        } else {
            return left;
        }
    };

    public static TypeEnvironment empty() {
        var scopes = Stacks.mutable.<TypingScope>empty();
        var typeSubst = UnionFind.<MonoType>of((l, r) -> pickTypeConstant(scopes, l, r));
        var kindSubst = UnionFind.<Kind>of((l, r) -> pickKindConstant(scopes, l, r));
        var sortTransformer = new SortSubstitutionTransformer(typeSubst, kindSubst);
        return new TypeEnvironment(scopes, typeSubst, kindSubst, sortTransformer);
    }

    public static TypeEnvironment of(TypingScope scope) {
        var env = TypeEnvironment.empty();
        env.scopes.push(scope);
        return env;
    }

    public static TypeEnvironment withBuiltInTypes() {
        return TypeEnvironment.of(BuiltInTypingScope.empty());
    }
}
