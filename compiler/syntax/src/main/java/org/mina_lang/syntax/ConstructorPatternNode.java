package org.mina_lang.syntax;

import java.util.Optional;

import org.eclipse.collections.api.list.ImmutableList;

public record ConstructorPatternNode(QualifiedIdNode id, Optional<String> alias, ImmutableList<FieldPatternNode> fields) implements PatternNode {
    
}
