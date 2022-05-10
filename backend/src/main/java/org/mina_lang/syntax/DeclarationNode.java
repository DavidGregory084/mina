package org.mina_lang.syntax;

sealed public interface DeclarationNode extends SyntaxNode permits DataDeclarationNode, LetDeclarationNode {
    
}
