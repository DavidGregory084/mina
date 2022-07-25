package org.mina_lang.syntax;

import java.util.Optional;

public record QualifiedIdNode<A> (Meta<A> meta, Optional<ModuleIdNode<Void>> mod, String name) implements SyntaxNode<A> {

}
