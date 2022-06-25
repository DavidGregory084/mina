package org.mina_lang.syntax;

public record ReferenceNode<A>(Meta<A> meta, QualifiedIdNode<A> id) implements ExprNode<A> {

}
