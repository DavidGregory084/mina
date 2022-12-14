package org.mina_lang.syntax;

import java.util.Optional;

import org.eclipse.collections.api.list.ImmutableList;
import org.mina_lang.common.Meta;

public record BlockNode<A> (Meta<A> meta, ImmutableList<LetNode<A>> declarations, Optional<ExprNode<A>> result)
        implements ExprNode<A> {
    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        declarations.forEach(decl -> decl.accept(visitor));
        result.ifPresent(visitor::visitExpr);
        visitor.visitBlock(this);
    }

    @Override
    public <B> B accept(MetaNodeFolder<A, B> visitor) {
        visitor.preVisitBlock(this);

        var result = visitor.visitBlock(
                meta(),
                declarations().collect(let -> let.accept(visitor)),
                result().map(visitor::visitExpr));

        visitor.postVisitBlock(result);

        return result;
    }

    @Override
    public <B> BlockNode<B> accept(MetaNodeTransformer<A, B> visitor) {
        visitor.preVisitBlock(this);

        var result = visitor.visitBlock(
                meta(),
                declarations().collect(let -> let.accept(visitor)),
                result().map(visitor::visitExpr));

        visitor.postVisitBlock(result);

        return result;
    }
}
