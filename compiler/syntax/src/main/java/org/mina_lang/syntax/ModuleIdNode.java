package org.mina_lang.syntax;

import org.eclipse.collections.api.list.ImmutableList;

public record ModuleIdNode<A> (Meta<A> meta, ImmutableList<String> pkg, String mod) implements SyntaxNode<A> {

}
