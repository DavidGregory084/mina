package org.mina_lang.typechecker;

import java.util.Comparator;

import org.eclipse.collections.api.factory.SortedSets;
import org.eclipse.collections.api.set.sorted.ImmutableSortedSet;
import org.mina_lang.common.types.*;

public class FreeUnsolvedVariablesFolder implements TypeFolder<ImmutableSortedSet<UnsolvedType>> {

    private static Comparator<UnsolvedType> COMPARATOR = Comparator.comparing(UnsolvedType::id);

    private TypeEnvironment environment;

    public FreeUnsolvedVariablesFolder(TypeEnvironment environment) {
        this.environment = environment;
    }

    @Override
    public ImmutableSortedSet<UnsolvedType> visitTypeLambda(TypeLambda tyLam) {
        return tyLam.body().accept(this);
    }

    @Override
    public ImmutableSortedSet<UnsolvedType> visitPropositionType(PropositionType propType) {
        return propType.type().accept(this);
    }

    @Override
    public ImmutableSortedSet<UnsolvedType> visitImplicationType(ImplicationType implType) {
        return implType.impliedType().accept(this);
    }

    @Override
    public ImmutableSortedSet<UnsolvedType> visitTypeConstructor(TypeConstructor tyCon) {
        return SortedSets.immutable.empty(COMPARATOR);
    }

    @Override
    public ImmutableSortedSet<UnsolvedType> visitBuiltInType(BuiltInType primTy) {
        return SortedSets.immutable.empty(COMPARATOR);
    }

    @Override
    public ImmutableSortedSet<UnsolvedType> visitTypeApply(TypeApply tyApp) {
        return tyApp.type().accept(this)
                .newWithAll(tyApp.typeArguments().flatCollect(tyArg -> tyArg.accept(this)));
    }

    @Override
    public ImmutableSortedSet<UnsolvedType> visitForAllVar(ForAllVar forall) {
        return SortedSets.immutable.empty(COMPARATOR);
    }

    @Override
    public ImmutableSortedSet<UnsolvedType> visitExistsVar(ExistsVar exists) {
        return SortedSets.immutable.empty(COMPARATOR);
    }

    @Override
    public ImmutableSortedSet<UnsolvedType> visitUnsolvedType(UnsolvedType unsolved) {
        var substituted = unsolved.substitute(environment.typeSubstitution(), environment.kindSubstitution());

        if (substituted.equals(unsolved)) {
            return SortedSets.immutable.of(COMPARATOR, unsolved);
        } else {
            return substituted.accept(this);
        }
    }
}
