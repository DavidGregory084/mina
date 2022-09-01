package org.mina_lang.syntax;

import java.util.Optional;

import org.eclipse.collections.api.list.ImmutableList;

public interface MetaNodeTransformer<A, B> {

    // Namespaces
    NamespaceNode<B> visitNamespace(Meta<A> meta, NamespaceIdNode id, ImmutableList<ImportNode> imports,
            ImmutableList<DeclarationNode<B>> declarations);

    // Declarations
    default DeclarationNode<B> visitDeclaration(DeclarationNode<A> decl) {
        return decl.accept(this);
    }

    DataNode<B> visitData(Meta<A> meta, String name, ImmutableList<TypeVarNode<B>> typeParams,
            ImmutableList<ConstructorNode<B>> constructors);

    ConstructorNode<B> visitConstructor(Meta<A> meta, String name, ImmutableList<ConstructorParamNode<B>> params,
            Optional<TypeNode<B>> type);

    ConstructorParamNode<B> visitConstructorParam(Meta<A> meta, String name, TypeNode<B> typeAnnotation);

    LetNode<B> visitLet(Meta<A> meta, String name, Optional<TypeNode<B>> type, ExprNode<B> expr);

    LetFnNode<B> visitLetFn(Meta<A> meta, String name, ImmutableList<TypeVarNode<B>> typeParams,
            ImmutableList<ParamNode<B>> valueParams, Optional<TypeNode<B>> returnType, ExprNode<B> expr);

    ParamNode<B> visitParam(Meta<A> param, String name, Optional<TypeNode<B>> typeAnnotation);

    // Types
    default TypeNode<B> visitType(TypeNode<A> typ) {
        return typ.accept(this);
    }

    TypeLambdaNode<B> visitTypeLambda(Meta<A> meta, ImmutableList<TypeVarNode<B>> args, TypeNode<B> body);

    FunTypeNode<B> visitFunType(Meta<A> meta, ImmutableList<TypeNode<B>> argTypes, TypeNode<B> returnType);

    TypeApplyNode<B> visitTypeApply(Meta<A> meta, TypeNode<B> type, ImmutableList<TypeNode<B>> args);

    TypeReferenceNode<B> visitTypeReference(Meta<A> meta, QualifiedIdNode<B> id);

    default TypeVarNode<B> visitTypeVar(TypeVarNode<A> tyVar) {
        return tyVar.accept(this);
    }

    ForAllVarNode<B> visitForAllVar(Meta<A> meta, String name);

    ExistsVarNode<B> visitExistsVar(Meta<A> meta, String name);

    // Expressions
    default ExprNode<B> visitExpr(ExprNode<A> expr) {
        return expr.accept(this);
    }

    BlockNode<B> visitBlock(Meta<A> meta, ImmutableList<LetNode<B>> declarations, ExprNode<B> result);

    IfNode<B> visitIf(Meta<A> meta, ExprNode<B> condition, ExprNode<B> consequence, ExprNode<B> alternative);

    LambdaNode<B> visitLambda(Meta<A> meta, ImmutableList<ParamNode<B>> params, ExprNode<B> body);

    MatchNode<B> visitMatch(Meta<A> meta, ExprNode<B> scrutinee, ImmutableList<CaseNode<B>> cases);

    ApplyNode<B> visitApply(Meta<A> meta, ExprNode<B> expr, ImmutableList<ExprNode<B>> args);

    ReferenceNode<B> visitReference(Meta<A> meta, QualifiedIdNode<B> id);

    default LiteralNode<B> visitLiteral(LiteralNode<A> literal) {
        return literal.accept(this);
    }

    BooleanNode<B> visitBoolean(Meta<A> meta, boolean value);

    CharNode<B> visitChar(Meta<A> meta, char value);

    StringNode<B> visitString(Meta<A> meta, String value);

    IntNode<B> visitInt(Meta<A> meta, int value);

    LongNode<B> visitLong(Meta<A> meta, long value);

    FloatNode<B> visitFloat(Meta<A> meta, float value);

    DoubleNode<B> visitDouble(Meta<A> meta, double value);

    // Cases and patterns
    CaseNode<B> visitCase(Meta<A> meta, PatternNode<B> pattern, ExprNode<B> consequent);

    default PatternNode<B> visitPattern(PatternNode<A> pat) {
        return pat.accept(this);
    }

    ConstructorPatternNode<B> visitConstructorPattern(Meta<A> meta, Optional<String> alias, QualifiedIdNode<B> id,
            ImmutableList<FieldPatternNode<B>> fields);

    FieldPatternNode<B> visitFieldPattern(Meta<A> meta, String field, Optional<PatternNode<B>> pattern);

    IdPatternNode<B> visitIdPattern(Meta<A> meta, Optional<String> alias, String name);

    LiteralPatternNode<B> visitLiteralPattern(Meta<A> meta, Optional<String> alias, LiteralNode<B> literal);

    // Identifiers
    QualifiedIdNode<B> visitQualifiedId(Meta<A> meta, Optional<NamespaceIdNode> ns, String name);
}
