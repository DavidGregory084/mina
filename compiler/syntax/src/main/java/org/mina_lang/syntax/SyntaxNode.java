package org.mina_lang.syntax;

import org.mina_lang.common.Range;

public interface SyntaxNode {
   Range range();
   void accept(SyntaxNodeVisitor visitor);
}
