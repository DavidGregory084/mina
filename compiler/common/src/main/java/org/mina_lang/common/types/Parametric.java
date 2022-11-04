package org.mina_lang.common.types;

import org.eclipse.collections.api.list.ImmutableList;

public record Parametric(ImmutableList<Kind> argKinds) implements Kind {

}
