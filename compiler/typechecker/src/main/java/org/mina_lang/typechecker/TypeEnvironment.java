/*
 * SPDX-FileCopyrightText:  © 2022-2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.typechecker;

import org.mina_lang.common.Attributes;
import org.mina_lang.common.Environment;
import org.mina_lang.common.names.Named;
import org.mina_lang.common.types.*;
import org.mina_lang.typechecker.scopes.*;

import java.util.*;

public record TypeEnvironment(
        Deque<TypingScope> scopes,
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
        return scopes().stream()
            .filter(scope -> scope instanceof NamespaceTypingScope)
            .findFirst()
            .map(scope -> (NamespaceTypingScope) scope);
    }

    public Optional<DataTypingScope> enclosingData() {
        return scopes().stream()
            .filter(scope -> scope instanceof DataTypingScope)
            .findFirst()
            .map(scope -> (DataTypingScope) scope);
    }

    public Optional<ConstructorTypingScope> enclosingConstructor() {
        return scopes().stream()
            .filter(scope -> scope instanceof ConstructorTypingScope)
            .findFirst()
            .map(scope -> (ConstructorTypingScope) scope);
    }

    public Optional<LambdaTypingScope> enclosingLambda() {
        return scopes().stream()
            .filter(scope -> scope instanceof LambdaTypingScope)
            .findFirst()
            .map(scope -> (LambdaTypingScope) scope);
    }

    public Optional<CaseTypingScope> enclosingCase() {
        return scopes().stream()
            .filter(scope -> scope instanceof CaseTypingScope)
            .findFirst()
            .map(scope -> (CaseTypingScope) scope);
    }

    public Optional<BlockTypingScope> enclosingBlock() {
        return scopes().stream()
            .filter(scope -> scope instanceof BlockTypingScope)
            .findFirst()
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

    public static Kind pickKindConstant(Deque<TypingScope> scopes, Kind left, Kind right) {
        if (left instanceof UnsolvedKind unsolvedLeft) {
            if (right instanceof UnsolvedKind unsolvedRight) {
                var leftDepth = -1;
                var rightDepth = -1;
                int i = 0;
                for (var it = scopes.iterator(); it.hasNext(); i++) {
                    var scope = it.next();
                    if (scope.unsolvedKinds().contains(unsolvedLeft)) {
                        leftDepth = i;
                    }
                    if (scope.unsolvedKinds().contains(unsolvedRight)) {
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

    public static MonoType pickTypeConstant(Deque<TypingScope> scopes, MonoType left, MonoType right) {
        if (left instanceof UnsolvedType unsolvedLeft) {
            if (right instanceof UnsolvedType unsolvedRight) {
                var leftDepth = -1;
                var rightDepth = -1;
                int i = 0;
                for (var it = scopes.iterator(); it.hasNext(); i++) {
                    var scope = it.next();
                    if (scope.unsolvedTypes().contains(unsolvedLeft)) {
                        leftDepth = i;
                    }
                    if (scope.unsolvedTypes().contains(unsolvedRight)) {
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
        var scopes = new ArrayDeque<TypingScope>();
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
