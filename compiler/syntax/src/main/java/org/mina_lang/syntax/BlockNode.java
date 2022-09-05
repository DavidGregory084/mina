package org.mina_lang.syntax;

import org.eclipse.collections.api.list.ImmutableList;
import org.mina_lang.common.Meta;

public record BlockNode<A> (Meta<A> meta, ImmutableList<LetNode<A>> declarations, ExprNode<A> result)
        implements ExprNode<A> {
    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        declarations.forEach(decl -> decl.accept(visitor));
        result.accept(visitor);
        visitor.visitBlock(this);
    }

    @Override
    public <B> B accept(MetaNodeVisitor<A, B> visitor) {
        return visitor.visitBlock(
                meta(),
                declarations().collect(let -> let.accept(visitor)),
                visitor.visitExpr(result()));
    }

    @Override
    public <B> BlockNode<B> accept(MetaNodeTransformer<A, B> transformer) {
        return transformer.visitBlock(
                meta(),
                declarations().collect(let -> let.accept(transformer)),
                transformer.visitExpr(result()));
    }
}
