package org.mina_lang.syntax;

import org.eclipse.collections.api.list.ImmutableList;

public record CompilationUnitNode<A>(Meta<A> meta, ImmutableList<ModuleNode<A>> modules) implements MetaNode<A> {
    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        modules.forEach(mod -> mod.accept(visitor));
        visitor.visitCompilationUnit(this);
    }
}
