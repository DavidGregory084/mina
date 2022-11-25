package org.mina_lang.syntax;

import static org.mina_lang.syntax.SyntaxNodes.*;

import org.eclipse.collections.api.list.ImmutableList;
import org.mina_lang.common.Attributes;
import org.mina_lang.common.Meta;
import org.mina_lang.common.types.Kind;
import org.mina_lang.common.types.KindSubstitutionTransformer;

public class TypeNodeSubstitutionTransformer implements TypeNodeTransformer<Attributes, Attributes> {

    private KindSubstitutionTransformer kindTransformer;

    public TypeNodeSubstitutionTransformer(KindSubstitutionTransformer kindTransformer) {
        this.kindTransformer = kindTransformer;
    }

    public Meta<Attributes> substituteMeta(Meta<Attributes> meta) {
        var updatedKind = ((Kind) meta.meta().sort()).accept(kindTransformer);
        var attributes = meta.meta().withSort(updatedKind);
        return meta.withMeta(attributes);
    }

    @Override
    public TypeLambdaNode<Attributes> visitTypeLambda(Meta<Attributes> meta,
            ImmutableList<TypeVarNode<Attributes>> args, TypeNode<Attributes> body) {
        return typeLambdaNode(substituteMeta(meta), args, body);
    }

    @Override
    public FunTypeNode<Attributes> visitFunType(Meta<Attributes> meta, ImmutableList<TypeNode<Attributes>> argTypes,
            TypeNode<Attributes> returnType) {
        return funTypeNode(substituteMeta(meta), argTypes, returnType);
    }

    @Override
    public TypeApplyNode<Attributes> visitTypeApply(Meta<Attributes> meta, TypeNode<Attributes> type,
            ImmutableList<TypeNode<Attributes>> args) {
        return typeApplyNode(substituteMeta(meta), type, args);
    }

    @Override
    public TypeReferenceNode<Attributes> visitTypeReference(Meta<Attributes> meta, QualifiedIdNode id) {
        return typeRefNode(substituteMeta(meta), id);
    }

    @Override
    public ForAllVarNode<Attributes> visitForAllVar(Meta<Attributes> meta, String name) {
        return forAllVarNode(substituteMeta(meta), name);
    }

    @Override
    public ExistsVarNode<Attributes> visitExistsVar(Meta<Attributes> meta, String name) {
        return existsVarNode(substituteMeta(meta), name);
    }
}
