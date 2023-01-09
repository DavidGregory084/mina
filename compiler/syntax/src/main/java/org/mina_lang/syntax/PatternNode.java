package org.mina_lang.syntax;

public sealed interface PatternNode<A>
        extends MetaNode<A>permits AliasPatternNode, ConstructorPatternNode, IdPatternNode, LiteralPatternNode {

    <B> B accept(PatternNodeFolder<A, B> folder);

    <B> PatternNode<B> accept(PatternNodeTransformer<A, B> transformer);

    @Override
    default <B> B accept(MetaNodeFolder<A, B> folder) {
        return accept((PatternNodeFolder<A, B>) folder);
    }

    @Override
    default <B> PatternNode<B> accept(MetaNodeTransformer<A, B> transformer) {
        return accept((PatternNodeTransformer<A, B>) transformer);
    }
}
