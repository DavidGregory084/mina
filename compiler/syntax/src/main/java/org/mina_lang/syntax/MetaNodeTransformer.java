package org.mina_lang.syntax;

import java.util.Optional;

import org.eclipse.collections.api.list.ImmutableList;
import org.mina_lang.common.Meta;

public interface MetaNodeTransformer<A, B> {

    // Namespaces
    default void preVisitNamespace(NamespaceNode<A> namespace) {}

    NamespaceNode<B> visitNamespace(Meta<A> meta, NamespaceIdNode id, ImmutableList<ImportNode> imports,
            ImmutableList<DeclarationNode<B>> declarations);

    default void postVisitNamespace(NamespaceNode<B> namespace) {}

    // Declarations
    default DeclarationNode<B> visitDeclaration(DeclarationNode<A> decl) {
        return decl.accept(this);
    }

    default void preVisitData(DataNode<A> data) {}

    DataNode<B> visitData(Meta<A> meta, String name, ImmutableList<TypeVarNode<B>> typeParams,
            ImmutableList<ConstructorNode<B>> constructors);

    default void postVisitData(DataNode<B> data) {}


    default void preVisitConstructor(ConstructorNode<A> constr) {}

    ConstructorNode<B> visitConstructor(Meta<A> meta, String name, ImmutableList<ConstructorParamNode<B>> params,
            Optional<TypeNode<B>> type);

    default void postVisitConstructor(ConstructorNode<B> constr) {}


    default void preVisitConstructorParam(ConstructorParamNode<A> constrParam) {}

    ConstructorParamNode<B> visitConstructorParam(Meta<A> meta, String name, TypeNode<B> typeAnnotation);

    default void postVisitConstructorParam(ConstructorParamNode<B> constrParam) {}


    default void preVisitLet(LetNode<A> let) {}

    LetNode<B> visitLet(Meta<A> meta, String name, Optional<TypeNode<B>> type, ExprNode<B> expr);

    default void postVisitLet(LetNode<B> let) {}


    default void preVisitLetFn(LetFnNode<A> letFn) {}

    LetFnNode<B> visitLetFn(Meta<A> meta, String name, ImmutableList<TypeVarNode<B>> typeParams,
            ImmutableList<ParamNode<B>> valueParams, Optional<TypeNode<B>> returnType, ExprNode<B> expr);

    default void postVisitLetFn(LetFnNode<B> letFn) {}


    default void preVisitParam(ParamNode<A> param) {}

    ParamNode<B> visitParam(Meta<A> param, String name, Optional<TypeNode<B>> typeAnnotation);

    default void postVisitParam(ParamNode<B> param) {}

    // Types
    default TypeNode<B> visitType(TypeNode<A> typ) {
        return typ.accept(this);
    }

    default void preVisitTypeLambda(TypeLambdaNode<A> tyLam) {}

    TypeLambdaNode<B> visitTypeLambda(Meta<A> meta, ImmutableList<TypeVarNode<B>> args, TypeNode<B> body);

    default void postVisitTypeLambda(TypeLambdaNode<B> tyLam) {}


    default void preVisitFunType(FunTypeNode<A> funTyp) {}

    FunTypeNode<B> visitFunType(Meta<A> meta, ImmutableList<TypeNode<B>> argTypes, TypeNode<B> returnType);

    default void postVisitFunType(FunTypeNode<B> funTyp) {}


    default void preVisitTypeApply(TypeApplyNode<A> tyApp) {}

    TypeApplyNode<B> visitTypeApply(Meta<A> meta, TypeNode<B> type, ImmutableList<TypeNode<B>> args);

    default void postVisitTypeApply(TypeApplyNode<B> tyApp) {}


    default void preVisitTypeReference(TypeReferenceNode<A> tyRef) {}

    TypeReferenceNode<B> visitTypeReference(Meta<A> meta, QualifiedIdNode id);

    default void postVisitTypeReference(TypeReferenceNode<B> tyRef) {}


    default TypeVarNode<B> visitTypeVar(TypeVarNode<A> tyVar) {
        return tyVar.accept(this);
    }


    default void preVisitForAllVar(ForAllVarNode<A> forAllVar) {}

    ForAllVarNode<B> visitForAllVar(Meta<A> meta, String name);

    default void postVisitForAllVar(ForAllVarNode<B> forAllVar) {}


    default void preVisitExistsVar(ExistsVarNode<A> existsVar) {}

    ExistsVarNode<B> visitExistsVar(Meta<A> meta, String name);

    default void postVisitExistsVar(ExistsVarNode<B> existsVar) {}

    // Expressions
    default ExprNode<B> visitExpr(ExprNode<A> expr) {
        return expr.accept(this);
    }

    default void preVisitBlock(BlockNode<A> block) {}

    BlockNode<B> visitBlock(Meta<A> meta, ImmutableList<LetNode<B>> declarations, ExprNode<B> result);

    default void postVisitBlock(BlockNode<B> block) {}


    default void preVisitIf(IfNode<A> ifExpr) {}

    IfNode<B> visitIf(Meta<A> meta, ExprNode<B> condition, ExprNode<B> consequent, ExprNode<B> alternative);

    default void postVisitIf(IfNode<B> ifExpr) {}


    default void preVisitLambda(LambdaNode<A> lambda) {}

    LambdaNode<B> visitLambda(Meta<A> meta, ImmutableList<ParamNode<B>> params, ExprNode<B> body);

    default void postVisitLambda(LambdaNode<B> lambda) {}


    default void preVisitMatch(MatchNode<A> match) {}

    MatchNode<B> visitMatch(Meta<A> meta, ExprNode<B> scrutinee, ImmutableList<CaseNode<B>> cases);

    default void postVisitMatch(MatchNode<B> match) {}


    default void preVisitApply(ApplyNode<A> apply) {}

    ApplyNode<B> visitApply(Meta<A> meta, ExprNode<B> expr, ImmutableList<ExprNode<B>> args);

    default void postVisitApply(ApplyNode<B> apply) {}


    default void preVisitReference(ReferenceNode<A> ref) {}

    ReferenceNode<B> visitReference(Meta<A> meta, QualifiedIdNode id);

    default void postVisitReference(ReferenceNode<B> ref) {}


    default LiteralNode<B> visitLiteral(LiteralNode<A> literal) {
        return literal.accept(this);
    }


    default void preVisitBoolean(BooleanNode<A> bool) {}

    BooleanNode<B> visitBoolean(Meta<A> meta, boolean value);

    default void postVisitBoolean(BooleanNode<B> bool) {}


    default void preVisitChar(CharNode<A> chr) {}

    CharNode<B> visitChar(Meta<A> meta, char value);

    default void postVisitChar(CharNode<B> chr) {}


    default void preVisitString(StringNode<A> str) {}

    StringNode<B> visitString(Meta<A> meta, String value);

    default void postVisitString(StringNode<B> str) {}


    default void preVisitInt(IntNode<A> intgr) {}

    IntNode<B> visitInt(Meta<A> meta, int value);

    default void postVisitInt(IntNode<B> intgr) {}


    default void preVisitLong(LongNode<A> lng) {}

    LongNode<B> visitLong(Meta<A> meta, long value);

    default void postVisitLong(LongNode<B> lng) {}


    default void preVisitFloat(FloatNode<A> flt) {}

    FloatNode<B> visitFloat(Meta<A> meta, float value);

    default void postVisitFloat(FloatNode<B> flt) {}


    default void preVisitDouble(DoubleNode<A> dbl) {}

    DoubleNode<B> visitDouble(Meta<A> meta, double value);

    default void postVisitDouble(DoubleNode<B> dbl) {}


    // Cases and patterns
    default void preVisitCase(CaseNode<A> cse) {}

    CaseNode<B> visitCase(Meta<A> meta, PatternNode<B> pattern, ExprNode<B> consequent);

    default void postVisitCase(CaseNode<B> cse) {}


    default PatternNode<B> visitPattern(PatternNode<A> pat) {
        return pat.accept(this);
    }

    default void preVisitAliasPattern(AliasPatternNode<A> alias) {}

    AliasPatternNode<B> visitAliasPattern(Meta<A> meta, String alias, PatternNode<B> pattern);

    default void postVisitAliasPattern(AliasPatternNode<B> alias) {}


    default void preVisitConstructorPattern(ConstructorPatternNode<A> constrPat) {}

    ConstructorPatternNode<B> visitConstructorPattern(Meta<A> meta, QualifiedIdNode id,
            ImmutableList<FieldPatternNode<B>> fields);

    default void postVisitConstructorPattern(ConstructorPatternNode<B> constrPat) {}


    default void preVisitFieldPattern(FieldPatternNode<A> fieldPat) {}

    FieldPatternNode<B> visitFieldPattern(Meta<A> meta, String field, Optional<PatternNode<B>> pattern);

    default void postVisitFieldPattern(FieldPatternNode<B> fieldPat) {}


    default void preVisitIdPattern(IdPatternNode<A> idPat) {}

    IdPatternNode<B> visitIdPattern(Meta<A> meta, String name);

    default void postVisitIdPattern(IdPatternNode<B> idPat) {}


    default void preVisitLiteralPattern(LiteralPatternNode<A> litPat) {}

    LiteralPatternNode<B> visitLiteralPattern(Meta<A> meta, LiteralNode<B> literal);

    default void postVisitLiteralPattern(LiteralPatternNode<B> litPat) {}
}
