package org.mina_lang.syntax;

import java.util.Optional;

public record ParamNode<A>(Meta<A> meta, String name, Optional<TypeNode<Void>> typeAnnotation) implements SyntaxNode<A> {

}
