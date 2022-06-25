package org.mina_lang.syntax;

import org.eclipse.lsp4j.Range;

public record Meta<A>(Range range, A meta) {

}
