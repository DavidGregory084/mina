package org.mina_lang.syntax;

public record StringNode<A>(Meta<A> meta, String value) implements LiteralNode<A> {
    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        visitor.visitString(this);
    }
}
