package org.mina_lang.syntax;

import org.eclipse.collections.api.list.ImmutableList;

public record CompilationUnitNode(ImmutableList<ModuleNode> modules) implements SyntaxNode {
    
}
