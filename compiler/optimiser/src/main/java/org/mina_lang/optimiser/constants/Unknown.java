/*
 * SPDX-FileCopyrightText:  © 2026 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.optimiser.constants;

public enum Unknown implements Result {
    VALUE;

    @Override
    public float compare(Result other) {
        return other == Unknown.VALUE ? 0.0F : -1.0F;
    }

    @Override
    public String toString() {
        return "Unknown";
    }
}
