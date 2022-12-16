package org.mina_lang.syntax;

public sealed interface PatternNode<A>
        extends MetaNode<A>permits AliasPatternNode, ConstructorPatternNode, IdPatternNode, LiteralPatternNode {

    <B> PatternNode<B> accept(PatternNodeTransformer<A, B> transformer);

    @Override
    default <B> PatternNode<B> accept(MetaNodeTransformer<A, B> transformer) {
        return accept((PatternNodeTransformer<A, B>) transformer);
    }
}
