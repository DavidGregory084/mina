package org.mina_lang.syntax;

import org.eclipse.collections.api.list.ImmutableList;

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
    public <B> B accept(MetaNodeVisitor<A, B> visitor) {
        return visitor.visitNamespace(
                meta(),
                id(),
                imports(),
                declarations().collect(visitor::visitDeclaration));
    }

    @Override
    public <B> NamespaceNode<B> accept(MetaNodeTransformer<A, B> transformer) {
        return transformer.visitNamespace(
                meta(),
                id(),
                imports(),
                declarations().collect(transformer::visitDeclaration));
    }
}
