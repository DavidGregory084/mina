package org.mina_lang.syntax;

import org.eclipse.collections.api.list.ImmutableList;
import org.mina_lang.common.*;
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
}
