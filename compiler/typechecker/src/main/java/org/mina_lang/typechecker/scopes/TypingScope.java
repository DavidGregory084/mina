/*
 * SPDX-FileCopyrightText:  © 2022-2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.typechecker.scopes;

import org.mina_lang.common.Attributes;
import org.mina_lang.common.Scope;
import org.mina_lang.common.types.SyntheticVar;
import org.mina_lang.common.types.UnsolvedKind;
import org.mina_lang.common.types.UnsolvedType;

import java.util.Set;

public interface TypingScope extends Scope<Attributes> {
    Set<SyntheticVar> syntheticVars();

    Set<UnsolvedKind> unsolvedKinds();

    Set<UnsolvedType> unsolvedTypes();
}
