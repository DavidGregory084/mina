/*
 * SPDX-FileCopyrightText:  © 2022-2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.common.types;

import com.opencastsoftware.prettier4j.Doc;

public class TypePrinter implements TypeFolder<Doc> {

    @Override
    public Doc visitTypeLambda(TypeLambda tyLam) {
        var argDoc = tyLam.args().size() == 1
                ? visitType(tyLam.args().get(0))
                : Doc.intersperse(
                        Doc.text(",").append(Doc.lineOrSpace()),
                        tyLam.args().stream().map(this::visitType))
                        .bracket(2, Doc.lineOrEmpty(), Doc.text("["), Doc.text("]"));

        return Doc.group(
                argDoc
                        .appendSpace(Doc.text("=>"))
                        .append(Doc.lineOrSpace().append(visitType(tyLam.body())).indent(2)));
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
        if (Type.isFunction(tyApp)) {
            var argTypes = tyApp.typeArguments().take(tyApp.typeArguments().size() - 1);
            var returnType = tyApp.typeArguments().getLast();

            var argDoc = argTypes.size() == 1
                    ? Type.isFunction(argTypes.get(0))
                            ? visitType(argTypes.get(0)).bracket(2, Doc.lineOrEmpty(), Doc.text("("), Doc.text(")"))
                            : visitType(argTypes.get(0))
                    : Doc.intersperse(
                            Doc.text(",").append(Doc.lineOrSpace()),
                            argTypes.stream().map(this::visitType))
                            .bracket(2, Doc.lineOrEmpty(), Doc.text("("), Doc.text(")"));

            return Doc.group(
                    argDoc
                            .appendSpace(Doc.text("->"))
                            .append(Doc.lineOrSpace().append(visitType(returnType)).indent(2)));
        } else {
            var appliedType = tyApp.type().accept(this);

            Doc typeArgs = Doc.intersperse(
                    Doc.text(",").append(Doc.lineOrSpace()),
                    tyApp.typeArguments().stream().map(this::visitType))
                    .bracket(2, Doc.lineOrEmpty(), Doc.text("["), Doc.text("]"));

            return appliedType.append(typeArgs);
        }
    }

    @Override
    public Doc visitExistsVar(ExistsVar exists) {
        return Doc.text(exists.name());
    }

    @Override
    public Doc visitForAllVar(ForAllVar forall) {
        return Doc.text(forall.name());
    }

    @Override
    public Doc visitUnsolvedType(UnsolvedType unsolved) {
        return Doc.text(unsolved.name());
    }
}
