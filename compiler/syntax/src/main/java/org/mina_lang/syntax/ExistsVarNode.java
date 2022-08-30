package org.mina_lang.syntax;

public record ExistsVarNode<A>(Meta<A> meta, String name) implements TypeVarNode<A> {
    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        visitor.visitExistsVar(this);
    }
}
