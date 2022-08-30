package org.mina_lang.syntax;

import java.util.Optional;

public record QualifiedIdNode<A> (Meta<A> meta, Optional<NamespaceIdNode<Void>> mod, String name) implements MetaNode<A> {
    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        mod.ifPresent(id -> id.accept(visitor));
        visitor.visitQualifiedId(this);
    }
}
