package org.mina_lang.syntax;

import java.util.Optional;

public record LetNode<A>(Meta<A> meta, String name, Optional<TypeNode<A>> type, ExprNode<A> expr) implements DeclarationNode<A> {
    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        type.ifPresent(typ -> typ.accept(visitor));
        expr.accept(visitor);
        visitor.visitLet(this);
    }
}
