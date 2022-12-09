package org.mina_lang.syntax;

import java.util.Optional;

import org.eclipse.collections.api.list.ImmutableList;
import org.mina_lang.common.Meta;

import static org.mina_lang.syntax.SyntaxNodes.*;

public interface MetaNodeMetaTransformer<A, B> extends MetaNodeTransformer<A, B>, DataNodeMetaTransformer<A, B>, PatternNodeMetaTransformer<A, B> {

    // Namespaces
    @Override
    default NamespaceNode<B> visitNamespace(Meta<A> meta, NamespaceIdNode id, ImmutableList<ImportNode> imports,
            ImmutableList<DeclarationNode<B>> declarations) {
        return namespaceNode(updateMeta(meta), id, imports, declarations);
    }


    // Declarations
    @Override
    default LetNode<B> visitLet(Meta<A> meta, String name, Optional<TypeNode<B>> type, ExprNode<B> expr) {
        return letNode(updateMeta(meta), name, type, expr);
    }

    @Override
    default LetFnNode<B> visitLetFn(Meta<A> meta, String name, ImmutableList<TypeVarNode<B>> typeParams,
            ImmutableList<ParamNode<B>> valueParams, Optional<TypeNode<B>> returnType, ExprNode<B> expr) {
        return letFnNode(updateMeta(meta), name, typeParams, valueParams, returnType, expr);
    }

    @Override
    default ParamNode<B> visitParam(Meta<A> param, String name, Optional<TypeNode<B>> typeAnnotation) {
        return paramNode(updateMeta(param), name, typeAnnotation);
    }

    // Expressions
    @Override
    default BlockNode<B> visitBlock(Meta<A> meta, ImmutableList<LetNode<B>> declarations, ExprNode<B> result) {
        return blockNode(updateMeta(meta), declarations, result);
    }

    @Override
    default IfNode<B> visitIf(Meta<A> meta, ExprNode<B> condition, ExprNode<B> consequent, ExprNode<B> alternative) {
        return ifNode(updateMeta(meta), condition, consequent, alternative);
    }

    @Override
    default LambdaNode<B> visitLambda(Meta<A> meta, ImmutableList<ParamNode<B>> params, ExprNode<B> body) {
        return lambdaNode(updateMeta(meta), params, body);
    }

    @Override
    default MatchNode<B> visitMatch(Meta<A> meta, ExprNode<B> scrutinee, ImmutableList<CaseNode<B>> cases) {
        return matchNode(updateMeta(meta), scrutinee, cases);
    }

    @Override
    default ApplyNode<B> visitApply(Meta<A> meta, ExprNode<B> expr, ImmutableList<ExprNode<B>> args) {
        return applyNode(updateMeta(meta), expr, args);
    }

    @Override
    default ReferenceNode<B> visitReference(Meta<A> meta, QualifiedIdNode id) {
        return refNode(updateMeta(meta), id);
    }

    // Cases
    @Override
    default CaseNode<B> visitCase(Meta<A> meta, PatternNode<B> pattern, ExprNode<B> consequent) {
        return caseNode(updateMeta(meta), pattern, consequent);
    }
}
