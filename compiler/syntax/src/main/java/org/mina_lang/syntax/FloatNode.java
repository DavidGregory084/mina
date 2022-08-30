package org.mina_lang.syntax;

public record FloatNode<A>(Meta<A> meta, float value) implements LiteralNode<A> {
    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        visitor.visitFloat(this);
    }
}
