/*
 * SPDX-FileCopyrightText:  © 2022-2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.common.names;

import org.mina_lang.common.Attributes;
import org.mina_lang.common.types.Sort;

public sealed interface Name permits Named, Nameless {
    default Attributes withSort(Sort sort) {
        return new Attributes(this, sort);
    }
}
