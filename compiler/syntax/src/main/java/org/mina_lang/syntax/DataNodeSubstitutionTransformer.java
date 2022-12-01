package org.mina_lang.syntax;

import java.util.Optional;

import org.eclipse.collections.api.list.ImmutableList;
import org.mina_lang.common.Attributes;
import org.mina_lang.common.Meta;
import org.mina_lang.common.types.KindSubstitutionTransformer;

import static org.mina_lang.syntax.SyntaxNodes.*;

public class DataNodeSubstitutionTransformer extends TypeNodeSubstitutionTransformer implements DataNodeTransformer<Attributes, Attributes> {

    public DataNodeSubstitutionTransformer(KindSubstitutionTransformer kindTransformer) {
        super(kindTransformer);
    }

    @Override
    public DataNode<Attributes> visitData(Meta<Attributes> meta, String name,
            ImmutableList<TypeVarNode<Attributes>> typeParams,
            ImmutableList<ConstructorNode<Attributes>> constructors) {
        return dataNode(substituteMeta(meta), name, typeParams, constructors);
    }

    @Override
    public ConstructorNode<Attributes> visitConstructor(Meta<Attributes> meta, String name,
            ImmutableList<ConstructorParamNode<Attributes>> params, Optional<TypeNode<Attributes>> type) {
        return constructorNode(substituteMeta(meta), name, params, type);
    }

    @Override
    public ConstructorParamNode<Attributes> visitConstructorParam(Meta<Attributes> meta, String name,
            TypeNode<Attributes> typeAnnotation) {
        return constructorParamNode(substituteMeta(meta), name, typeAnnotation);
    }
}
