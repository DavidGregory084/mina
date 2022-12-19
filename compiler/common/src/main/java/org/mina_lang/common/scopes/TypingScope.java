package org.mina_lang.common.scopes;

import org.eclipse.collections.api.set.MutableSet;
import org.mina_lang.common.Attributes;
import org.mina_lang.common.types.UnsolvedKind;
import org.mina_lang.common.types.UnsolvedType;

public non-sealed interface TypingScope extends Scope<Attributes> {

    MutableSet<UnsolvedKind> unsolvedKinds();

    MutableSet<UnsolvedType> unsolvedTypes();
}
