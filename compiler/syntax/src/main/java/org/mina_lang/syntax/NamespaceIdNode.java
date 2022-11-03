package org.mina_lang.syntax;

import org.eclipse.collections.api.list.ImmutableList;
import org.mina_lang.common.names.NamespaceName;
import org.mina_lang.common.Range;

public record NamespaceIdNode (Range range, ImmutableList<String> pkg, String ns) implements SyntaxNode {
    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        visitor.visitNamespaceId(this);
    }

    public NamespaceName getName() {
        return new NamespaceName(pkg, ns);
    }
}
