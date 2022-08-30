package org.mina_lang.syntax;

import org.eclipse.collections.api.list.ImmutableList;

public record BlockNode<A>(Meta<A> meta, ImmutableList<LetNode<A>> declarations, ExprNode<A> result) implements ExprNode<A> {
    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        declarations.forEach(decl -> decl.accept(visitor));
        result.accept(visitor);
        visitor.visitBlock(this);
    }
}
