package org.mina_lang.syntax;

import org.eclipse.collections.api.list.ImmutableList;

public record ImportNode<A>(Meta<A> meta, ModuleIdNode<Void> mod, ImmutableList<String> symbols) implements SyntaxNode<A> {

}
