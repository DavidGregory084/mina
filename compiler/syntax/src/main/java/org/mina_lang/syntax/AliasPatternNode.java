package org.mina_lang.syntax;

import org.mina_lang.common.Meta;

public record AliasPatternNode<A> (Meta<A> meta, String alias, PatternNode<A> pattern) implements PatternNode<A> {

    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        pattern.accept(visitor);
        visitor.visitAliasPattern(this);
    }

    @Override
    public <B> B accept(MetaNodeFolder<A, B> visitor) {
        return visitor.visitAliasPattern(
                meta(),
                alias(),
                visitor.visitPattern(pattern()));
    }

    @Override
    public <B> PatternNode<B> accept(MetaNodeTransformer<A, B> visitor) {
        return visitor.visitAliasPattern(
                meta(),
                alias(),
                visitor.visitPattern(pattern()));
    }
}
