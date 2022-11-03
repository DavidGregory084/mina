package org.mina_lang.syntax;

import org.eclipse.collections.api.list.ImmutableList;
import org.mina_lang.common.*;
import org.mina_lang.common.names.NamespaceName;

public record NamespaceNode<A> (Meta<A> meta, NamespaceIdNode id, ImmutableList<ImportNode> imports,
        ImmutableList<DeclarationNode<A>> declarations) implements MetaNode<A> {

    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        id.accept(visitor);
        imports.forEach(imp -> imp.accept(visitor));
        declarations.forEach(decl -> decl.accept(visitor));
        visitor.visitNamespace(this);
    }

    @Override
    public <B> B accept(MetaNodeFolder<A, B> visitor) {
        visitor.preVisitNamespace(this);

        var result = visitor.visitNamespace(
                meta(),
                id(),
                imports(),
                declarations().collect(visitor::visitDeclaration));

        visitor.postVisitNamespace(result);

        return result;
    }

    @Override
    public <B> NamespaceNode<B> accept(MetaNodeTransformer<A, B> visitor) {
        visitor.preVisitNamespace(this);

        var result = visitor.visitNamespace(
                meta(),
                id(),
                imports(),
                declarations().collect(visitor::visitDeclaration));

        visitor.postVisitNamespace(result);

        return result;
    }

    public NamespaceName getName() {
        return id().getName();
    }
}
