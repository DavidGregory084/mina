package org.mina_lang.syntax;

import java.util.Optional;

import org.mina_lang.common.Meta;

public record QualifiedIdNode<A> (Meta<A> meta, Optional<NamespaceIdNode> ns, String name) implements MetaNode<A> {

    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        ns.ifPresent(id -> id.accept(visitor));
        visitor.visitQualifiedId(this);
    }

    @Override
    public <B> B accept(MetaNodeVisitor<A, B> visitor) {
        return visitor.visitQualifiedId(
            meta(),
            ns(),
            name()
        );
    }

    @Override
    public <B> QualifiedIdNode<B> accept(MetaNodeTransformer<A, B> transformer) {
        return transformer.visitQualifiedId(
            meta(),
            ns(),
            name()
        );
    }
}
