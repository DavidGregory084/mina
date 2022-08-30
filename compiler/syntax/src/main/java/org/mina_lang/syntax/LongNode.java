package org.mina_lang.syntax;

public record LongNode<A>(Meta<A> meta, long value) implements LiteralNode<A> {
    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        visitor.visitLong(this);
    }
}
