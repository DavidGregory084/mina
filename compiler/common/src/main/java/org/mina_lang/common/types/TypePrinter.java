/*
 * SPDX-FileCopyrightText:  © 2022-2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.common.types;

import com.opencastsoftware.prettier4j.Doc;

public class TypePrinter implements TypeFolder<Doc> {
    private static final Doc LSQUARE = Doc.text("[");
    private static final Doc RSQUARE = Doc.text("]");
    private static final Doc LPAREN = Doc.text("(");
    private static final Doc RPAREN = Doc.text(")");
    private static final Doc LBRACE = Doc.text("{");
    private static final Doc RBRACE = Doc.text("}");
    private static final Doc COMMA = Doc.text(",").append(Doc.lineOrSpace());
    private static final Doc ARROW = Doc.text("->");

    private static final int DEFAULT_INDENT = 3;

    private final int indent;

    public TypePrinter(int indent) {
        this.indent = indent;
    }

    public TypePrinter() {
        this(DEFAULT_INDENT);
    }

    @Override
    public Doc visitQuantifiedType(QuantifiedType quant) {
        var argDoc = Doc.intersperse(
                COMMA,
                quant.args().stream().map(this::visitType))
                .bracket(this.indent, Doc.lineOrEmpty(), LSQUARE, RSQUARE);

        return argDoc.appendSpace(
                    visitType(quant.body())
                        .bracket(this.indent, LBRACE, RBRACE));
    }

    @Override
    public Doc visitTypeConstructor(TypeConstructor tyCon) {
        return Doc.text(tyCon.name().name());
    }

    @Override
    public Doc visitBuiltInType(BuiltInType primTy) {
        return Doc.text(primTy.name());
    }

    @Override
    public Doc visitTypeApply(TypeApply tyApp) {
        if (Type.isFunction(tyApp)) {
            var argTypes = tyApp.typeArguments().subList(0, tyApp.typeArguments().size() - 1);
            var returnType = tyApp.typeArguments().get(tyApp.typeArguments().size() - 1);

            var argDoc = argTypes.size() == 1
                    ? Type.isFunction(argTypes.get(0))
                            ? visitType(argTypes.get(0)).bracket(this.indent, Doc.lineOrEmpty(), LPAREN, RPAREN)
                            : visitType(argTypes.get(0))
                    : Doc.intersperse(
                            COMMA,
                            argTypes.stream().map(this::visitType))
                            .bracket(this.indent, Doc.lineOrEmpty(), LPAREN, RPAREN);

            return Doc.group(
                    argDoc
                            .appendSpace(ARROW)
                            .append(Doc.lineOrSpace().append(visitType(returnType)).indent(this.indent)));
        } else {
            var appliedType = tyApp.type().accept(this);

            Doc typeArgs = Doc.intersperse(
                    Doc.text(",").append(Doc.lineOrSpace()),
                    tyApp.typeArguments().stream().map(this::visitType))
                    .bracket(this.indent, Doc.lineOrEmpty(), LSQUARE, RSQUARE);

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
    public Doc visitSyntheticVar(SyntheticVar syn) {
        return Doc.text(syn.name());
    }

    @Override
    public Doc visitUnsolvedType(UnsolvedType unsolved) {
        return Doc.text(unsolved.name());
    }
}
