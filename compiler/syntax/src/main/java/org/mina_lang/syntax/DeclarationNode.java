package org.mina_lang.syntax;

sealed public interface DeclarationNode<A> extends SyntaxNode<A> permits DataDeclarationNode<A>, LetDeclarationNode<A> {

}
