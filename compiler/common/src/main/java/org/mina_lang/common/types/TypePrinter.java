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
        return null;
    }

    @Override
    public Doc visitImplicationType(ImplicationType implType) {
        return null;
    }

    @Override
    public Doc visitTypeConstructor(TypeConstructor tyCon) {
        // TODO: Disambiguate names properly
        return Doc.text(tyCon.name().name());
    }

    @Override
    public Doc visitTypeApply(TypeApply tyApp) {
        var appliedType = tyApp.type().accept(this);

        Doc typeArgs = tyApp.typeArguments().isEmpty() ? Doc.empty()
                : tyApp.typeArguments().stream()
                        .map(ty -> ty.accept(this))
                        .reduce(Doc.empty(), (left, right) -> {
                            return left instanceof Doc.Empty ? right
                                    : left.append(Doc.text(",")).appendLineOrSpace(right);
                        })
                        .bracket(2, Doc.text("["), Doc.text("]"));

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
