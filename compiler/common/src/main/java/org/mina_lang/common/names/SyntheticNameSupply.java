/*
 * SPDX-FileCopyrightText:  © 2025 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.common.names;

import java.util.concurrent.atomic.AtomicInteger;

public class SyntheticNameSupply {
    private final AtomicInteger syntheticName = new AtomicInteger();

    public SyntheticName newSyntheticName() {
        return new SyntheticName(syntheticName.getAndIncrement());
    }
}
