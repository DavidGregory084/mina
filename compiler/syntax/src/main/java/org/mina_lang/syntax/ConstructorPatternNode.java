package org.mina_lang.syntax;

import org.eclipse.collections.api.list.ImmutableList;
import org.mina_lang.common.Meta;

public record ConstructorPatternNode<A>(Meta<A> meta, QualifiedIdNode id,
        ImmutableList<FieldPatternNode<A>> fields) implements PatternNode<A> {
    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        id.accept(visitor);
        fields.forEach(field -> field.accept(visitor));
        visitor.visitConstructorPattern(this);
    }

    @Override
    public <B> B accept(PatternNodeFolder<A, B> visitor) {
        visitor.preVisitConstructorPattern(this);

        var result = visitor.visitConstructorPattern(
                meta(),
                id(),
                fields().collect(field -> field.accept(visitor)));

        visitor.postVisitConstructorPattern(this);

        return result;
    }

    @Override
    public <B> ConstructorPatternNode<B> accept(PatternNodeTransformer<A, B> visitor) {
        visitor.preVisitConstructorPattern(this);

        var result = visitor.visitConstructorPattern(
                meta(),
                id(),
                fields().collect(field -> field.accept(visitor)));

        visitor.postVisitConstructorPattern(result);

        return result;
    }
}
