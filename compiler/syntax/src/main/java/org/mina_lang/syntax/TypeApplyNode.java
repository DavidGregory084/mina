package org.mina_lang.syntax;

import org.eclipse.collections.api.list.ImmutableList;

public record TypeApplyNode<A> (Meta<A> meta, TypeNode<A> type, ImmutableList<TypeNode<A>> args)
        implements TypeNode<A> {
    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        type.accept(visitor);
        args.forEach(arg -> arg.accept(visitor));
        visitor.visitTypeApply(this);
    }
}
