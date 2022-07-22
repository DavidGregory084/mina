package org.mina_lang.syntax;

public record UniversalTypeVarNode<A>(Meta<A> meta, String name) implements TypeVarNode<A> {

}
