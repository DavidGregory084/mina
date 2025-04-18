/*
 * SPDX-FileCopyrightText:  © 2022-2025 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.syntax;

public interface SyntaxNodeVisitor {
    default void visit(SyntaxNode node) {
    }

    // Namespaces
    default void visitNamespace(NamespaceNode<?> ns) {
        visit(ns);
    }

    // Imports
    default void visitImport(ImportNode imp) {
        imp.accept(this);
    }

    default void visitImportQualified(ImportQualifiedNode qual) {
        visit(qual);
    }

    default void visitImportSymbols(ImportSymbolsNode syms) {
        visit(syms);
    }

    default void visitImportee(ImporteeNode sym) {
        visit(sym);
    }

    // Declarations
    default void visitDeclaration(DeclarationNode<?> decl) {
        decl.accept(this);
    }

    default void visitData(DataNode<?> data) {
        visit(data);
    }

    default void visitConstructor(ConstructorNode<?> constr) {
        visit(constr);
    }

    default void visitConstructorParam(ConstructorParamNode<?> constrParam) {
        visit(constrParam);
    }

    default void visitLet(LetNode<?> let) {
        visit(let);
    }

    default void visitLetFn(LetFnNode<?> letFn) {
        visit(letFn);
    }

    default void visitParam(ParamNode<?> param) {
        visit(param);
    }

    // Types
    default void visitType(TypeNode<?> typ) {
        typ.accept(this);
    }

    default void visitFunType(FunTypeNode<?> funTyp) {
        visit(funTyp);
    }

    default void visitTypeApply(TypeApplyNode<?> tyApp) {
        visit(tyApp);
    }

    default void visitQuantifiedType(QuantifiedTypeNode<?> lam) {
        visit(lam);
    }

    default void visitTypeReference(TypeReferenceNode<?> tyRef) {
        visit(tyRef);
    }

    default void visitTypeVar(TypeVarNode<?> tyVar) {
        tyVar.accept(this);
    }

    default void visitForAllVar(ForAllVarNode<?> forAll) {
        visit(forAll);
    }

    default void visitExistsVar(ExistsVarNode<?> exists) {
        visit(exists);
    }

    // Expressions
    default void visitExpr(ExprNode<?> expr) {
        expr.accept(this);
    }

    default void visitBlock(BlockNode<?> block) {
        visit(block);
    }

    default void visitIf(IfNode<?> ifExpr) {
        visit(ifExpr);
    }

    default void visitLambda(LambdaNode<?> lambda) {
        visit(lambda);
    }

    default void visitMatch(MatchNode<?> match) {
        visit(match);
    }

    default void visitApply(ApplyNode<?> apply) {
        visit(apply);
    }

    default void visitSelect(SelectNode<?> select) {
        visit(select);
    }

    default void visitUnaryOpNode(UnaryOpNode<?> unOp) {
        visit(unOp);
    }

    default void visitBinaryOpNode(BinaryOpNode<?> binOp) {
        visit(binOp);
    }

    default void visitReference(ReferenceNode<?> ref) {
        visit(ref);
    }

    default void visitLiteral(LiteralNode<?> literal) {
        literal.accept(this);
    }

    default void visitBoolean(BooleanNode<?> bool) {
        visit(bool);
    }

    default void visitChar(CharNode<?> chr) {
        visit(chr);
    }

    default void visitString(StringNode<?> str) {
        visit(str);
    }

    default void visitInt(IntNode<?> intgr) {
        visit(intgr);
    }

    default void visitLong(LongNode<?> lng) {
        visit(lng);
    }

    default void visitFloat(FloatNode<?> flt) {
        visit(flt);
    }

    default void visitDouble(DoubleNode<?> dbl) {
        visit(dbl);
    }

    // Cases and patterns
    default void visitCase(CaseNode<?> cse) {
        visit(cse);
    }

    default void visitPattern(PatternNode<?> pat) {
        pat.accept(this);
    }

    default void visitAliasPattern(AliasPatternNode<?> aliasPat) {
        visit(aliasPat);
    }

    default void visitConstructorPattern(ConstructorPatternNode<?> constrPat) {
        visit(constrPat);
    }

    default void visitFieldPattern(FieldPatternNode<?> fieldPat) {
        visit(fieldPat);
    }

    default void visitIdPattern(IdPatternNode<?> idPat) {
        visit(idPat);
    }

    default void visitLiteralPattern(LiteralPatternNode<?> litPat) {
        visit(litPat);
    }

    // Identifiers
    default void visitNamespaceId(NamespaceIdNode id) {
        visit(id);
    }

    default void visitQualifiedId(QualifiedIdNode id) {
        visit(id);
    }

}
