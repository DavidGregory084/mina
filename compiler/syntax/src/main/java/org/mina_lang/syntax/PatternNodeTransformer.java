package org.mina_lang.syntax;

import java.util.Optional;

import org.eclipse.collections.api.list.ImmutableList;
import org.mina_lang.common.Meta;

public interface PatternNodeTransformer<A, B> extends LiteralNodeTransformer<A, B> {
    default PatternNode<B> visitPattern(PatternNode<A> pat) {
        return pat.accept(this);
    }

    default void preVisitAliasPattern(AliasPatternNode<A> alias) {}

    AliasPatternNode<B> visitAliasPattern(Meta<A> meta, String alias, PatternNode<B> pattern);

    default void postVisitAliasPattern(AliasPatternNode<B> alias) {}


    default void preVisitConstructorPattern(ConstructorPatternNode<A> constrPat) {}

    ConstructorPatternNode<B> visitConstructorPattern(Meta<A> meta, QualifiedIdNode id,
            ImmutableList<FieldPatternNode<B>> fields);

    default void postVisitConstructorPattern(ConstructorPatternNode<B> constrPat) {}


    default void preVisitFieldPattern(FieldPatternNode<A> fieldPat) {}

    FieldPatternNode<B> visitFieldPattern(Meta<A> meta, String field, Optional<PatternNode<B>> pattern);

    default void postVisitFieldPattern(FieldPatternNode<B> fieldPat) {}


    default void preVisitIdPattern(IdPatternNode<A> idPat) {}

    IdPatternNode<B> visitIdPattern(Meta<A> meta, String name);

    default void postVisitIdPattern(IdPatternNode<B> idPat) {}


    default void preVisitLiteralPattern(LiteralPatternNode<A> litPat) {}

    LiteralPatternNode<B> visitLiteralPattern(Meta<A> meta, LiteralNode<B> literal);

    default void postVisitLiteralPattern(LiteralPatternNode<B> litPat) {}
}
