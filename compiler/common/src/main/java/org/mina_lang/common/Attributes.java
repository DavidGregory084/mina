/*
 * SPDX-FileCopyrightText:  © 2022-2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.common;

import org.mina_lang.common.names.Name;
import org.mina_lang.common.types.Sort;

public record Attributes(Name name, Sort sort) {
    public Attributes withSort(Sort newSort) {
        return new Attributes(name(), newSort);
    }
}
