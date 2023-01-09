package org.mina_lang.syntax;

import java.util.Optional;

import org.eclipse.collections.api.list.ImmutableList;
import org.mina_lang.common.Meta;

public interface PatternNodeFolder<A, B> extends LiteralNodeFolder<A, B> {
    default B visitPattern(PatternNode<A> pat) {
        return pat.accept(this);
    }

    default void preVisitAliasPattern(AliasPatternNode<A> alias) {}

    B visitAliasPattern(Meta<A> meta, String alias, B pattern);

    default void postVisitAliasPattern(AliasPatternNode<A> alias) {}


    default void preVisitConstructorPattern(ConstructorPatternNode<A> constrPat) {}

    B visitConstructorPattern(Meta<A> meta, QualifiedIdNode id, ImmutableList<B> fields);

    default void postVisitConstructorPattern(ConstructorPatternNode<A> constrPat) {}


    default void preVisitFieldPattern(FieldPatternNode<A> fieldPat) {}

    B visitFieldPattern(Meta<A> meta, String field, Optional<B> pattern);

    default void postVisitFieldPattern(FieldPatternNode<A> fieldPat) {}


    default void preVisitIdPattern(IdPatternNode<A> idPat) {}

    B visitIdPattern(Meta<A> meta, String name);

    default void postVisitIdPattern(IdPatternNode<A> idPat) {}


    default void preVisitLiteralPattern(LiteralPatternNode<A> litPat) {}

    B visitLiteralPattern(Meta<A> meta, B literal);

    default void postVisitLiteralPattern(LiteralPatternNode<A> litPat) {}
}
