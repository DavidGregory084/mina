package org.mina_lang.syntax;

import java.util.Optional;

public record LetDeclarationNode<A>(Meta<A> meta, String name, Optional<TypeNode<Void>> type, ExprNode<A> expr) implements DeclarationNode<A> {

}
