package org.mina_lang.syntax;

public record IntNode<A>(Meta<A> meta, int value) implements LiteralNode<A> {
    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        visitor.visitInt(this);
    }
}
