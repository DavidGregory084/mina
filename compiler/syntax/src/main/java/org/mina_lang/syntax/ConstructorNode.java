package org.mina_lang.syntax;

import org.eclipse.collections.api.list.ImmutableList;
import org.mina_lang.common.*;

import java.util.Optional;

public record ConstructorNode<A> (Meta<A> meta, String name, ImmutableList<ConstructorParamNode<A>> params,
        Optional<TypeNode<A>> type) implements MetaNode<A> {
    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        params.forEach(param -> param.accept(visitor));
        type.ifPresent(typ -> typ.accept(visitor));
        visitor.visitConstructor(this);
    }

    @Override
    public <B> B accept(MetaNodeFolder<A, B> visitor) {
        visitor.preVisitConstructor(this);

        var result = visitor.visitConstructor(
                meta(),
                name(),
                params().collect(param -> param.accept(visitor)),
                type().map(visitor::visitType));

        visitor.postVisitConstructor(result);

        return result;
    }

    @Override
    public <B> ConstructorNode<B> accept(MetaNodeTransformer<A, B> visitor) {
        visitor.preVisitConstructor(this);

        var result = visitor.visitConstructor(
                meta(),
                name(),
                params().collect(param -> param.accept(visitor)),
                type().map(visitor::visitType));

        visitor.postVisitConstructor(result);

        return result;
    }

    public ConstructorName getName(DataName enclosing, NamespaceName namespace) {
        return new ConstructorName(enclosing, new QualifiedName(namespace, name));
    }
}
