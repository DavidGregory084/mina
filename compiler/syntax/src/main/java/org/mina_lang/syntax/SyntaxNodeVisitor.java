package org.mina_lang.syntax;

public interface SyntaxNodeVisitor {
    void visit(SyntaxNode node);

    // Namespaces
    default void visitNamespace(NamespaceNode<?> mod) {
        visit(mod);
    }

    // Imports
    default void visitImport(ImportNode<?> imp) {
        visit(imp);
    }

    // Declarations
    default void visitDeclaration(DeclarationNode<?> decl) {
        switch (decl) {
            case LetNode<?> let -> visitLet(let);
            case LetFnNode<?> letFn -> visitLetFn(letFn);
            case DataNode<?> data -> visitData(data);
        };
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
        switch (typ)  {
            case TypeLambdaNode<?> lam -> visitTypeLambda(lam);
            case FunTypeNode<?> funTyp -> visitFunType(funTyp);
            case TypeApplyNode<?> tyApp -> visitTypeApply(tyApp);
            case TypeVarNode<?> tyVar -> visitTypeVar(tyVar);
            case TypeReferenceNode<?> tyRef -> visitTypeReference(tyRef);
        };
    }

    default void visitFunType(FunTypeNode<?> funTyp) {
        visit(funTyp);
    }

    default void visitTypeApply(TypeApplyNode<?> tyApp) {
        visit(tyApp);
    }

    default void visitTypeLambda(TypeLambdaNode<?> lam) {
        visit(lam);
    }

    default void visitTypeReference(TypeReferenceNode<?> tyRef) {
        visit(tyRef);
    }

    default void visitTypeVar(TypeVarNode<?> tyVar) {
        switch (tyVar) {
            case ForAllVarNode<?> forAll -> visitForAllVar(forAll);
            case ExistsVarNode<?> exists -> visitExistsVar(exists);
        }
    }

    default void visitForAllVar(ForAllVarNode<?> forAll) {
        visit(forAll);
    }

    default void visitExistsVar(ExistsVarNode<?> exists) {
        visit(exists);
    }

    // Expressions
    default void visitExpr(ExprNode<?> expr) {
        switch (expr) {
            case BlockNode<?> block -> visitBlock(block);
            case IfNode<?> ifExpr -> visitIf(ifExpr);
            case LambdaNode<?> lambda -> visitLambda(lambda);
            case MatchNode<?> match -> visitMatch(match);
            case ApplyNode<?> apply -> visitApply(apply);
            case ReferenceNode<?> ref -> visitReference(ref);
            case LiteralNode<?> literal -> visitLiteral(literal);
        };
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

    default void visitReference(ReferenceNode<?> ref) {
        visit(ref);
    }

    default void visitLiteral(LiteralNode<?> literal) {
        switch (literal) {
            case BooleanNode<?> bool -> visitBoolean(bool);
            case CharNode<?> chr -> visitChar(chr);
            case DoubleNode<?> dbl -> visitDouble(dbl);
            case FloatNode<?> flt -> visitFloat(flt);
            case IntNode<?> intgr -> visitInt(intgr);
            case LongNode<?> lng -> visitLong(lng);
            case StringNode<?> str -> visitString(str);
        };
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
        switch (pat) {
            case ConstructorPatternNode<?> constrPat -> visitConstructorPattern(constrPat);
            case IdPatternNode<?> idPat -> visitIdPattern(idPat);
            case LiteralPatternNode<?> litPat -> visitLiteralPattern(litPat);
        };
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
    default void visitNamespaceId(NamespaceIdNode<?> id) {
        visit(id);
    }

    default void visitQualifiedId(QualifiedIdNode<?> id) {
        visit(id);
    }
}
