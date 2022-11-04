package org.mina_lang.common.types;

public interface TypeFolder<A> {
    default A visitType(Type type) {
        return type.accept(this);
    }

    default A visitPolyType(PolyType poly) {
        return poly.accept(this);
    }

    A visitForAllType(ForAllType forall);

    A visitExistsType(ExistsType exists);

    A visitPropositionType(PropositionType propType);

    A visitImplicationType(ImplicationType implType);

    default A visitMonoType(MonoType mono) {
        return mono.accept(this);
    }

    A visitTypeConstructor(TypeConstructor tyCon);

    A visitTypeApply(TypeApply tyApp);

    A visitTypeVar(TypeVar tyVar);

    A visitUnsolvedType(UnsolvedType unsolved);
}
