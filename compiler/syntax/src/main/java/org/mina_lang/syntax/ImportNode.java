package org.mina_lang.syntax;

import org.eclipse.collections.api.list.ImmutableList;

public record ImportNode<A>(Meta<A> meta, NamespaceIdNode<Void> mod, ImmutableList<String> symbols) implements MetaNode<A> {
    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        mod.accept(visitor);
        visitor.visitImport(this);
    }
}
