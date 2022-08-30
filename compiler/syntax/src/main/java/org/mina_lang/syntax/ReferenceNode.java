package org.mina_lang.syntax;

public record ReferenceNode<A>(Meta<A> meta, QualifiedIdNode<A> id) implements ExprNode<A> {
    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        id.accept(visitor);
        visitor.visitReference(this);
    }
}
