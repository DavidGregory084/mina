package org.mina_lang.syntax;

import org.eclipse.collections.api.list.ImmutableList;

public record DataNode<A> (Meta<A> meta, String name, ImmutableList<TypeVarNode<A>> typeParams,
        ImmutableList<ConstructorNode<A>> constructors) implements DeclarationNode<A> {
    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        typeParams.forEach(tyParam -> tyParam.accept(visitor));
        constructors.forEach(constr -> constr.accept(visitor));
        visitor.visitData(this);
    }

    @Override
    public <B> B accept(MetaNodeVisitor<A, B> visitor) {
        return visitor.visitData(
                meta(),
                name(),
                typeParams().collect(visitor::visitTypeVar),
                constructors().collect(constr -> constr.accept(visitor)));
    }

    @Override
    public <B> DataNode<B> accept(MetaNodeTransformer<A, B> transformer) {
        return transformer.visitData(
                meta(),
                name(),
                typeParams().collect(transformer::visitTypeVar),
                constructors().collect(constr -> constr.accept(transformer)));
    }
}
