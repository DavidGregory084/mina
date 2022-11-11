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

    BuiltInType visitBuiltInType(BuiltInType primTy);

    TypeApply visitTypeApply(TypeApply tyApp);

    TypeVar visitTypeVar(TypeVar tyVar);

    UnsolvedType visitUnsolvedType(UnsolvedType unsolved);
}
