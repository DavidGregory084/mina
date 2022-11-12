package org.mina_lang.common.types;

import com.opencastsoftware.prettier4j.Doc;

public class SortPrinter implements SortFolder<Doc> {

    private final KindPrinter kindPrint;
    private final TypePrinter typePrint;

    public SortPrinter(KindPrinter kindPrint, TypePrinter typePrint) {
        this.kindPrint = kindPrint;
        this.typePrint = typePrint;
    }

    @Override
    public Doc visitTypeKind(TypeKind typ) {
        return kindPrint.visitTypeKind(typ);
    }

    @Override
    public Doc visitUnsolvedKind(UnsolvedKind unsolved) {
        return kindPrint.visitUnsolvedKind(unsolved);
    }

    @Override
    public Doc visitHigherKind(HigherKind higher) {
        return kindPrint.visitHigherKind(higher);
    }

    @Override
    public Doc visitForAllType(ForAllType forall) {
        return typePrint.visitForAllType(forall);
    }

    @Override
    public Doc visitExistsType(ExistsType exists) {
        return typePrint.visitExistsType(exists);
    }

    @Override
    public Doc visitPropositionType(PropositionType propType) {
        return typePrint.visitPropositionType(propType);
    }

    @Override
    public Doc visitImplicationType(ImplicationType implType) {
        return typePrint.visitImplicationType(implType);
    }

    @Override
    public Doc visitTypeConstructor(TypeConstructor tyCon) {
        return typePrint.visitTypeConstructor(tyCon);
    }

    @Override
    public Doc visitBuiltInType(BuiltInType primTy) {
        return typePrint.visitBuiltInType(primTy);
    }

    @Override
    public Doc visitTypeApply(TypeApply tyApp) {
        return typePrint.visitTypeApply(tyApp);
    }

    @Override
    public Doc visitTypeVar(TypeVar tyVar) {
        return typePrint.visitTypeVar(tyVar);
    }

    @Override
    public Doc visitUnsolvedType(UnsolvedType unsolved) {
        return typePrint.visitUnsolvedType(unsolved);
    }
}
