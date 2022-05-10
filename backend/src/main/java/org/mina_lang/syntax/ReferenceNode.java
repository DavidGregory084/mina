package org.mina_lang.syntax;

import org.eclipse.collections.api.list.ImmutableList;

public record ReferenceNode(ImmutableList<String> pkg, String name) implements ExprNode {
    
}
