package org.mina_lang.common.types;

public interface TypeTransformer {
    Type visitType(Type type);

    PolyType visitPolyType(PolyType poly);

    ForAllType visitForAllType(ForAllType forall);

    ExistsType visitExistsType(ExistsType exists);

    PropositionType visitPropositionType(PropositionType propType);

    ImplicationType visitImplicationType(ImplicationType implType);

    MonoType visitMonoType(MonoType mono);

    TypeConstructor visitTypeConstructor(TypeConstructor tyCon);

    TypeApply visitTypeApply(TypeApply tyApp);

    TypeVar visitTypeVar(TypeVar tyVar);

    UnsolvedType visiUnsolvedType(UnsolvedType unsolved);
}
