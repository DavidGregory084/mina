package org.mina_lang.syntax;

import org.eclipse.collections.api.list.ImmutableList;

import java.util.Optional;

public record ConstructorPatternNode<A>(Meta<A> meta, Optional<String> alias, QualifiedIdNode<A> id, ImmutableList<FieldPatternNode<A>> fields) implements PatternNode<A> {
    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        id.accept(visitor);
        fields.forEach(field -> field.accept(visitor));
        visitor.visitConstructorPattern(this);
    }
}
