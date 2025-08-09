/*
 * SPDX-FileCopyrightText:  © 2022-2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.syntax;

import org.mina_lang.common.Meta;
import org.mina_lang.common.Scope;
import org.mina_lang.common.TopLevelScope;
import org.mina_lang.common.names.ConstructorName;
import org.mina_lang.common.names.NamespaceName;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record NamespaceNode<A> (Meta<A> meta, NamespaceIdNode id, List<ImportNode> imports,
        List<List<DeclarationNode<A>>> declarationGroups) implements MetaNode<A> {

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
                declarationGroups().stream().map(group -> group.stream().map(visitor::visitDeclaration).toList()).toList());

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
                declarationGroups().stream().map(group -> group.stream().map(visitor::visitDeclaration).toList()).toList());

        visitor.postVisitNamespace(result);

        return result;
    }

    public NamespaceName getName() {
        return id().getName();
    }

    public Scope<A> getScope() {
        var nsName = getName();

        Map<String, Meta<A>> values = new HashMap<>();
        Map<String, Meta<A>> types = new HashMap<>();
        Map<ConstructorName, Map<String, Meta<A>>> fields = new HashMap<>();

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
                        Map<String, Meta<A>> constrFields = new HashMap<>();
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
