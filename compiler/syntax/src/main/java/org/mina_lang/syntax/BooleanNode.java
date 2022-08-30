package org.mina_lang.syntax;

public record BooleanNode<A>(Meta<A> meta, boolean value) implements LiteralNode<A> {
    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        visitor.visitBoolean(this);
    }
}
