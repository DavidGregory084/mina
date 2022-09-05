package org.mina_lang.syntax;

import org.eclipse.collections.api.list.ImmutableList;
import org.mina_lang.common.Meta;

import java.util.Optional;

public record ConstructorPatternNode<A> (Meta<A> meta, Optional<String> alias, QualifiedIdNode<A> id,
        ImmutableList<FieldPatternNode<A>> fields) implements PatternNode<A> {
    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        id.accept(visitor);
        fields.forEach(field -> field.accept(visitor));
        visitor.visitConstructorPattern(this);
    }

    @Override
    public <B> B accept(MetaNodeVisitor<A, B> visitor) {
        return visitor.visitConstructorPattern(
                meta(),
                alias(),
                id().accept(visitor),
                fields().collect(field -> field.accept(visitor)));
    }

    @Override
    public <B> ConstructorPatternNode<B> accept(MetaNodeTransformer<A, B> transformer) {
        return transformer.visitConstructorPattern(
                meta(),
                alias(),
                id().accept(transformer),
                fields().collect(field -> field.accept(transformer)));
    }
}
