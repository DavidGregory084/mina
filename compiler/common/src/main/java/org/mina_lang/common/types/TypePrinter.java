package org.mina_lang.common.types;

import com.opencastsoftware.prettier4j.Doc;

public class TypePrinter implements TypeFolder<Doc> {

    @Override
    public Doc visitForAllType(ForAllType forall) {
        return null;
    }

    @Override
    public Doc visitExistsType(ExistsType exists) {
        return null;
    }

    @Override
    public Doc visitPropositionType(PropositionType propType) {
        // Don't tell users about propositional types
        return visitType(propType.type());
    }

    @Override
    public Doc visitImplicationType(ImplicationType implType) {
        // Don't tell users about implication types
        return visitType(implType.impliedType());
    }

    @Override
    public Doc visitTypeConstructor(TypeConstructor tyCon) {
        // TODO: Disambiguate names properly by accepting import environment in
        // constructor
        return Doc.text(tyCon.name().name());
    }

    @Override
    public Doc visitBuiltInType(BuiltInType primTy) {
        return Doc.text(primTy.name());
    }

    @Override
    public Doc visitTypeApply(TypeApply tyApp) {
        var appliedType = tyApp.type().accept(this);

        Doc typeArgs = Doc.intersperse(
                Doc.text(",").append(Doc.lineOrSpace()),
                tyApp.typeArguments().stream().map(ty -> ty.accept(this)))
                .bracket(2, Doc.lineOrEmpty(), Doc.text("["), Doc.text("]"));

        return appliedType.append(typeArgs);
    }

    @Override
    public Doc visitTypeVar(TypeVar tyVar) {
        return Doc.text(tyVar.name());
    }

    @Override
    public Doc visitUnsolvedType(UnsolvedType unsolved) {
        return Doc.text(unsolved.name());
    }
}
