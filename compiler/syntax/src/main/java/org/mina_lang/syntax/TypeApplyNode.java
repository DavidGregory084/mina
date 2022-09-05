package org.mina_lang.syntax;

import org.eclipse.collections.api.list.ImmutableList;
import org.mina_lang.common.Meta;

public record TypeApplyNode<A> (Meta<A> meta, TypeNode<A> type, ImmutableList<TypeNode<A>> args)
        implements TypeNode<A> {

    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        type.accept(visitor);
        args.forEach(arg -> arg.accept(visitor));
        visitor.visitTypeApply(this);
    }

    @Override
    public <B> B accept(MetaNodeVisitor<A, B> visitor) {
        return visitor.visitTypeApply(
            meta(),
            visitor.visitType(type()),
            args().collect(visitor::visitType)
        );
    }

    @Override
    public <B> TypeApplyNode<B> accept(MetaNodeTransformer<A, B> transformer) {
        return transformer.visitTypeApply(
            meta(),
            transformer.visitType(type()),
            args().collect(transformer::visitType)
        );
    }
}
