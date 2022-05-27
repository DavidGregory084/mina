package org.mina_lang.syntax;

import org.eclipse.collections.api.list.ImmutableList;

public record ImportNode(ImmutableList<String> pkg, String mod, ImmutableList<String> symbols) implements SyntaxNode {

}
