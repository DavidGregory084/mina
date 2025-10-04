/*
 * SPDX-FileCopyrightText:  © 2022-2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.typechecker.scopes;

import org.eclipse.collections.api.set.MutableSet;
import org.mina_lang.common.Attributes;
import org.mina_lang.common.Meta;
import org.mina_lang.common.Scope;
import org.mina_lang.common.types.SyntheticVar;
import org.mina_lang.common.types.UnsolvedKind;
import org.mina_lang.common.types.UnsolvedType;

public interface TypingScope extends Scope<Meta<Attributes>> {
    MutableSet<SyntheticVar> syntheticVars();

    MutableSet<UnsolvedKind> unsolvedKinds();

    MutableSet<UnsolvedType> unsolvedTypes();
}
