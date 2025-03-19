/*
 * SPDX-FileCopyrightText:  © 2022-2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
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
    public Doc visitQuantifiedType(QuantifiedType quant) {
        return typePrint.visitQuantifiedType(quant);
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
    public Doc visitForAllVar(ForAllVar forall) {
        return typePrint.visitForAllVar(forall);
    }

    @Override
    public Doc visitExistsVar(ExistsVar exists) {
        return typePrint.visitExistsVar(exists);
    }

    @Override
    public Doc visitSyntheticVar(SyntheticVar syn) {
        return typePrint.visitSyntheticVar(syn);
    }

    @Override
    public Doc visitUnsolvedType(UnsolvedType unsolved) {
        return typePrint.visitUnsolvedType(unsolved);
    }
}
