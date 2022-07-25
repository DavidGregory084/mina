package org.mina_lang.syntax;

import org.eclipse.collections.api.list.ImmutableList;

public record ModuleNode<A>(Meta<A> meta, ModuleIdNode<Void> id, ImmutableList<ImportNode<Void>> imports, ImmutableList<DeclarationNode<A>> declarations) implements SyntaxNode<A> {
}
