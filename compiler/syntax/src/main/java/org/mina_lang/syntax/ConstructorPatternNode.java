package org.mina_lang.syntax;

import java.util.Optional;

import org.eclipse.collections.api.list.ImmutableList;

public record ConstructorPatternNode<A>(Meta<A> meta, QualifiedIdNode<A> id, Optional<String> alias, ImmutableList<FieldPatternNode<A>> fields) implements PatternNode<A> {

}
