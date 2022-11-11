package org.mina_lang.common.types;

import org.eclipse.collections.api.list.ImmutableList;

public record HigherKind(ImmutableList<Kind> argKinds, Kind resultKind) implements Kind {

}
