package org.mina_lang.syntax;

public record CharNode<A>(Meta<A> meta, char value) implements LiteralNode<A> {
    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        visitor.visitChar(this);
    }
}
