package org.mina_lang.syntax;

import org.eclipse.collections.api.list.ImmutableList;

public record ModuleNode(ImmutableList<String> pkg, String name, ImmutableList<DeclarationNode> declarations) implements SyntaxNode {
}
