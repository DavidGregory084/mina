package org.mina_lang.syntax;

import java.util.Optional;

import org.mina_lang.common.Range;
import org.mina_lang.common.names.NamespaceName;
import org.mina_lang.common.names.QualifiedName;

public record QualifiedIdNode (Range range, Optional<NamespaceIdNode> ns, String name) implements SyntaxNode {

    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        ns.ifPresent(id -> id.accept(visitor));
        visitor.visitQualifiedId(this);
    }

    public String canonicalName() {
        return ns().map(ns -> ns.getName().canonicalName() + ".").orElse("") + name();
    }

    public QualifiedName getName(NamespaceName currentNamespace) {
        return new QualifiedName(
            ns.map(NamespaceIdNode::getName).orElse(currentNamespace),
            name);
    }
}
