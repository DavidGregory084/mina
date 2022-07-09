package org.mina_lang.syntax;

import java.util.Optional;

public record IdPatternNode<A> (Meta<A> meta, Optional<String> alias, String name) implements PatternNode<A> {

}
