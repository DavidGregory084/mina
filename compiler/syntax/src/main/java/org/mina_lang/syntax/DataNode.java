/*
 * SPDX-FileCopyrightText:  © 2022-2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.syntax;

import org.mina_lang.common.Meta;
import org.mina_lang.common.names.DataName;
import org.mina_lang.common.names.NamespaceName;
import org.mina_lang.common.names.QualifiedName;

import java.util.List;

public record DataNode<A> (Meta<A> meta, String name, List<TypeVarNode<A>> typeParams,
        List<ConstructorNode<A>> constructors) implements DeclarationNode<A> {
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
                typeParams().stream().map(visitor::visitTypeVar).toList(),
                constructors().stream().map(constr -> constr.accept(visitor)).toList());

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
                typeParams().stream().map(visitor::visitTypeVar).toList(),
                constructors().stream().map(constr -> constr.accept(visitor)).toList());

        visitor.postVisitData(result);

        return result;
    }

    public DataName getName(NamespaceName namespace) {
        return new DataName(new QualifiedName(namespace, name));
    }
}
