package org.mina_lang.syntax;

public record ForAllVarNode<A>(Meta<A> meta, String name) implements TypeVarNode<A> {

}
