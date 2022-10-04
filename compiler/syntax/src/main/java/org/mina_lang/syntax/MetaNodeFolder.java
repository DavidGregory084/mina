package org.mina_lang.syntax;

import java.util.Optional;

import org.eclipse.collections.api.list.ImmutableList;
import org.mina_lang.common.Meta;

public interface MetaNodeFolder<A, B> {

    // Namespaces
    default void preVisitNamespace(NamespaceNode<A> namespace) {}

    B visitNamespace(Meta<A> meta, NamespaceIdNode id, ImmutableList<ImportNode> imports,
            ImmutableList<B> declarations);

    default void postVisitNamespace(B namespace) {}

    // Declarations
    default B visitDeclaration(DeclarationNode<A> decl) {
        return decl.accept(this);
    }

    default void preVisitData(DataNode<A> data) {}

    B visitData(Meta<A> meta, String name, ImmutableList<B> typeParams, ImmutableList<B> constructors);

    default void postVisitData(B data) {}


    default void preVisitConstructor(ConstructorNode<A> constr) {}

    B visitConstructor(Meta<A> meta, String name, ImmutableList<B> params, Optional<B> type);

    default void postVisitConstructor(B constr) {}


    default void preVisitConstructorParam(ConstructorParamNode<A> constrParam) {}

    B visitConstructorParam(Meta<A> meta, String name, B typeAnnotation);

    default void postVisitConstructorParam(B constrParam) {}


    default void preVisitLet(LetNode<A> let) {}

    B visitLet(Meta<A> meta, String name, Optional<B> type, B expr);

    default void postVisitLet(B let) {}


    default void preVisitLetFn(LetFnNode<A> letFn) {}

    B visitLetFn(Meta<A> meta, String name, ImmutableList<B> typeParams, ImmutableList<B> valueParams,
            Optional<B> returnType, B expr);

    default void postVisitLetFn(B letFn) {}


    default void preVisitParam(ParamNode<A> param) {}

    B visitParam(Meta<A> param, String name, Optional<B> typeAnnotation);

    default void postVisitParam(B param) {}

    // Types
    default B visitType(TypeNode<A> typ) {
        return typ.accept(this);
    }

    default void preVisitTypeLambda(TypeLambdaNode<A> tyLam) {}

    B visitTypeLambda(Meta<A> meta, ImmutableList<B> args, B body);

    default void postVisitTypeLambda(B tyLam) {}


    default void preVisitFunType(FunTypeNode<A> funTyp) {}

    B visitFunType(Meta<A> meta, ImmutableList<B> argTypes, B returnType);

    default void postVisitFunType(B funTyp) {}


    default void preVisitTypeApply(TypeApplyNode<A> tyApp) {}

    B visitTypeApply(Meta<A> meta, B type, ImmutableList<B> args);

    default void postVisitTypeApply(B tyApp) {}


    default void preVisitTypeReference(TypeReferenceNode<A> tyRef) {}

    B visitTypeReference(Meta<A> meta, QualifiedIdNode id);

    default void postVisitTypeReference(B tyRef) {}


    default B visitTypeVar(TypeVarNode<A> tyVar) {
        return tyVar.accept(this);
    }

    default void preVisitForAllVar(ForAllVarNode<A> forAllVar) {}

    B visitForAllVar(Meta<A> meta, String name);

    default void postVisitForAllVar(B forAllVar) {}


    default void preVisitExistsVar(ExistsVarNode<A> existsVar) {}

    B visitExistsVar(Meta<A> meta, String name);

    default void postVisitExistsVar(B existsVar) {}

    // Expressions
    default B visitExpr(ExprNode<A> expr) {
        return expr.accept(this);
    }

    default void preVisitBlock(BlockNode<A> block) {}

    B visitBlock(Meta<A> meta, ImmutableList<B> declarations, B result);

    default void postVisitBlock(B block) {}


    default void preVisitIf(IfNode<A> ifExpr) {}

    B visitIf(Meta<A> meta, B condition, B consequent, B alternative);

    default void postVisitIf(B ifExpr) {}


    default void preVisitLambda(LambdaNode<A> lambda) {}

    B visitLambda(Meta<A> meta, ImmutableList<B> params, B body);

    default void postVisitLambda(B lambda) {}


    default void preVisitMatch(MatchNode<A> match) {}

    B visitMatch(Meta<A> meta, B scrutinee, ImmutableList<B> cases);

    default void postVisitMatch(B match) {}


    default void preVisitApply(ApplyNode<A> apply) {}

    B visitApply(Meta<A> meta, B expr, ImmutableList<B> args);

    default void postVisitApply(B apply) {}


    default void preVisitReference(ReferenceNode<A> ref) {}

    B visitReference(Meta<A> meta, QualifiedIdNode id);

    default void postVisitReference(B ref) {}


    default B visitLiteral(LiteralNode<A> literal) {
        return literal.accept(this);
    }

    default void preVisitBoolean(BooleanNode<A> bool) {}

    B visitBoolean(Meta<A> meta, boolean value);

    default void postVisitBoolean(B bool) {}


    default void preVisitChar(CharNode<A> chr) {}

    B visitChar(Meta<A> meta, char value);

    default void postVisitChar(B chr) {}


    default void preVisitString(StringNode<A> str) {}

    B visitString(Meta<A> meta, String value);

    default void postVisitString(B str) {}


    default void preVisitInt(IntNode<A> intgr) {}

    B visitInt(Meta<A> meta, int value);

    default void postVisitInt(B intgr) {}


    default void preVisitLong(LongNode<A> lng) {}

    B visitLong(Meta<A> meta, long value);

    default void postVisitLong(B lng) {}


    default void preVisitFloat(FloatNode<A> flt) {}

    B visitFloat(Meta<A> meta, float value);

    default void postVisitFloat(B flt) {}


    default void preVisitDouble(DoubleNode<A> dbl) {}

    B visitDouble(Meta<A> meta, double value);

    default void postVisitDouble(B dbl) {}

    // Cases and patterns
    default void preVisitCase(CaseNode<A> cse) {}

    B visitCase(Meta<A> meta, B pattern, B consequent);

    default void postVisitCase(B cse) {}


    default B visitPattern(PatternNode<A> pat) {
        return pat.accept(this);
    }

    default void preVisitAliasPattern(AliasPatternNode<A> alias) {}

    B visitAliasPattern(Meta<A> meta, String alias, B pattern);

    default void postVisitAliasPattern(B alias) {}


    default void preVisitConstructorPattern(ConstructorPatternNode<A> constrPat) {}

    B visitConstructorPattern(Meta<A> meta, QualifiedIdNode id, ImmutableList<B> fields);

    default void postVisitConstructorPattern(B constrPat) {}


    default void preVisitFieldPattern(FieldPatternNode<A> fieldPat) {}

    B visitFieldPattern(Meta<A> meta, String field, Optional<B> pattern);

    default void postVisitFieldPattern(B fieldPat) {}


    default void preVisitIdPattern(IdPatternNode<A> idPat) {}

    B visitIdPattern(Meta<A> meta, String name);

    default void postVisitIdPattern(B idPat) {}


    default void preVisitLiteralPattern(LiteralPatternNode<A> litPat) {}

    B visitLiteralPattern(Meta<A> meta, B literal);

    default void postVisitLiteralPattern(B litPat) {}
}
