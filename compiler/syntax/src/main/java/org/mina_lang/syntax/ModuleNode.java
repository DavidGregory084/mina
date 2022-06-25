package org.mina_lang.syntax;

import org.eclipse.collections.api.list.ImmutableList;

public record ModuleNode<A>(Meta<A> meta, ImmutableList<String> pkg, String name, ImmutableList<ImportNode<A>> imports, ImmutableList<DeclarationNode<A>> declarations) implements SyntaxNode<A> {
}
