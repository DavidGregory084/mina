package org.mina_lang.syntax;

import org.eclipse.collections.api.list.ImmutableList;

public record ModuleNode<A>(Meta<A> meta, ModuleIdNode<Void> id, ImmutableList<ImportNode<Void>> imports, ImmutableList<DeclarationNode<A>> declarations) implements MetaNode<A> {
    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        id.accept(visitor);
        imports.forEach(imp -> imp.accept(visitor));
        declarations.forEach(decl -> decl.accept(visitor));
        visitor.visitModule(this);
    }
}
