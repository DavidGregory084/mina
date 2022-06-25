package org.mina_lang.syntax;

import org.eclipse.collections.api.list.ImmutableList;

public record QualifiedIdNode<A>(Meta<A> meta, ImmutableList<String> pkg, String name) implements SyntaxNode<A> {

}
