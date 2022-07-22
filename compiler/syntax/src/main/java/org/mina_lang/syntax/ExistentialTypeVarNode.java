package org.mina_lang.syntax;

public record ExistentialTypeVarNode<A>(Meta<A> meta, String name) implements TypeVarNode<A> {
}
