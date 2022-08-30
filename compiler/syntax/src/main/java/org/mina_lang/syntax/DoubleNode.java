package org.mina_lang.syntax;

public record DoubleNode<A>(Meta<A> meta, double value) implements LiteralNode<A> {
    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        visitor.visitDouble(this);
    }
}
