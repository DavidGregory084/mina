package org.mina_lang.syntax;

import org.eclipse.collections.api.list.ImmutableList;

public record MatchNode(ExprNode scrutinee, ImmutableList<CaseNode> cases) implements ExprNode {
    
}
