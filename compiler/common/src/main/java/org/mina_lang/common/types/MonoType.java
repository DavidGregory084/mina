/*
 * SPDX-FileCopyrightText:  © 2022-2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.common.types;

public sealed interface MonoType extends Type permits TypeConstructor, BuiltInType, TypeApply, TypeVar, UnsolvedType {

    @Override
    MonoType accept(TypeTransformer visitor);
}
