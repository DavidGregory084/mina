package org.mina_lang.syntax;

import com.opencastsoftware.yvette.Range;

public interface SyntaxNode {
   Range range();
   void accept(SyntaxNodeVisitor visitor);
}
