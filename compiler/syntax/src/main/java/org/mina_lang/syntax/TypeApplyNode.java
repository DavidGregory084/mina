package org.mina_lang.syntax;

import org.eclipse.collections.api.list.ImmutableList;
import org.mina_lang.common.Meta;

public record TypeApplyNode<A>(Meta<A> meta, TypeNode<A> type, ImmutableList<TypeNode<A>> args)
        implements TypeNode<A> {

    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        type.accept(visitor);
        args.forEach(arg -> arg.accept(visitor));
        visitor.visitTypeApply(this);
    }

    @Override
    public <B> B accept(TypeNodeFolder<A, B> visitor) {
        visitor.preVisitTypeApply(this);

        var result = visitor.visitTypeApply(
                meta(),
                visitor.visitType(type()),
                args().collect(visitor::visitType));

        visitor.postVisitTypeApply(result);

        return result;
    }

    @Override
    public <B> TypeApplyNode<B> accept(TypeNodeTransformer<A, B> visitor) {
        visitor.preVisitTypeApply(this);

        var result = visitor.visitTypeApply(
                meta(),
                visitor.visitType(type()),
                args().collect(visitor::visitType));

        visitor.postVisitTypeApply(result);

        return result;
    }
}
