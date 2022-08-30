package org.mina_lang.syntax;

public record CaseNode<A>(Meta<A> meta, PatternNode<A> pattern, ExprNode<A> consequent) implements MetaNode<A> {
    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        pattern.accept(visitor);
        consequent.accept(visitor);
        visitor.visitCase(this);
    }
}
