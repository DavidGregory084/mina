/*
 * SPDX-FileCopyrightText:  © 2022-2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.syntax;

import org.eclipse.collections.api.list.ImmutableList;
import org.mina_lang.common.Meta;
import org.mina_lang.common.names.DataName;
import org.mina_lang.common.names.NamespaceName;
import org.mina_lang.common.names.QualifiedName;

public record DataNode<A> (Meta<A> meta, String name, ImmutableList<TypeVarNode<A>> typeParams,
        ImmutableList<ConstructorNode<A>> constructors) implements DeclarationNode<A> {
    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        typeParams.forEach(tyParam -> tyParam.accept(visitor));
        constructors.forEach(constr -> constr.accept(visitor));
        visitor.visitData(this);
    }

    @Override
    public <B> B accept(MetaNodeFolder<A, B> visitor) {
        return accept((DataNodeFolder<A, B>) visitor);
    }

    public <B> B accept(DataNodeFolder<A, B> visitor) {
        visitor.preVisitData(this);

        var result = visitor.visitData(
                meta(),
                name(),
                typeParams().collect(visitor::visitTypeVar),
                constructors().collect(constr -> constr.accept(visitor)));

        visitor.postVisitData(result);

        return result;
    }

    @Override
    public <B> DataNode<B> accept(MetaNodeTransformer<A, B> visitor) {
        return accept((DataNodeTransformer<A, B>) visitor);
    }

    public <B> DataNode<B> accept(DataNodeTransformer<A, B> visitor) {
        visitor.preVisitData(this);

        var result = visitor.visitData(
                meta(),
                name(),
                typeParams().collect(visitor::visitTypeVar),
                constructors().collect(constr -> constr.accept(visitor)));

        visitor.postVisitData(result);

        return result;
    }

    public DataName getName(NamespaceName namespace) {
        return new DataName(new QualifiedName(namespace, name));
    }
}
