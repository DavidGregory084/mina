/*
 * SPDX-FileCopyrightText:  © 2022-2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.syntax;

import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.map.MutableMap;
import org.mina_lang.common.Meta;
import org.mina_lang.common.Scope;
import org.mina_lang.common.TopLevelScope;
import org.mina_lang.common.names.ConstructorName;
import org.mina_lang.common.names.NamespaceName;

public record NamespaceNode<A> (Meta<A> meta, NamespaceIdNode id, ImmutableList<ImportNode> imports,
        ImmutableList<ImmutableList<DeclarationNode<A>>> declarationGroups) implements MetaNode<A> {

    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        id.accept(visitor);
        imports.forEach(imp -> imp.accept(visitor));
        declarationGroups.forEach(group -> group.forEach(visitor::visitDeclaration));
        visitor.visitNamespace(this);
    }

    @Override
    public <B> B accept(MetaNodeFolder<A, B> visitor) {
        visitor.preVisitNamespace(this);

        var result = visitor.visitNamespace(
                meta(),
                id(),
                imports(),
                declarationGroups().collect(group -> group.collect(visitor::visitDeclaration)));

        visitor.postVisitNamespace(this);

        return result;
    }

    @Override
    public <B> NamespaceNode<B> accept(MetaNodeTransformer<A, B> visitor) {
        visitor.preVisitNamespace(this);

        var result = visitor.visitNamespace(
                meta(),
                id(),
                imports(),
                declarationGroups().collect(group -> group.collect(visitor::visitDeclaration)));

        visitor.postVisitNamespace(result);

        return result;
    }

    public NamespaceName getName() {
        return id().getName();
    }

    public Scope<Meta<A>> getScope() {
        var nsName = getName();

        MutableMap<String, Meta<A>> values = Maps.mutable.empty();
        MutableMap<String, Meta<A>> types = Maps.mutable.empty();
        MutableMap<ConstructorName, MutableMap<String, Meta<A>>> fields = Maps.mutable.empty();

        declarationGroups.forEach(group -> {
            group.forEach(declaration -> {
                if (declaration instanceof LetNode<A> let) {
                    values.put(let.name(), let.meta());
                } else if (declaration instanceof LetFnNode<A> letFn) {
                    values.put(letFn.name(), letFn.meta());
                } else if (declaration instanceof DataNode<A> data) {
                    types.put(data.name(), data.meta());

                    data.constructors().forEach(constr -> {
                        values.put(constr.name(), constr.meta());

                        var constrName = constr.getName(data.getName(nsName), nsName);
                        MutableMap<String, Meta<A>> constrFields = Maps.mutable.empty();
                        constr.params().forEach(param -> {
                            constrFields.put(param.name(), param.meta());
                        });

                        fields.put(constrName, constrFields);
                    });
                }
            });
        });

        return new TopLevelScope<>(values, types, fields);
    }
}
