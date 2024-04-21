/*
 * SPDX-FileCopyrightText:  © 2022-2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.common.types;

public sealed interface PolyType extends Type permits QuantifiedType, PropositionType, ImplicationType {
    @Override
    PolyType accept(TypeTransformer visitor);
}
