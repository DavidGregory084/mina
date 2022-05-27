package org.mina_lang.syntax;

import org.eclipse.collections.api.list.ImmutableList;

public record ApplyNode(ExprNode expr, ImmutableList<ExprNode> args) implements ExprNode {

}
