package org.mina_lang.syntax;

public record CaseNode(PatternNode pattern, ExprNode consequent) implements SyntaxNode {
    
}
