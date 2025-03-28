/*
 * SPDX-FileCopyrightText:  © 2022-2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.common.types;

import java.util.concurrent.atomic.AtomicInteger;

public class UnsolvedVariableSupply {
    private final AtomicInteger unsolvedType = new AtomicInteger();
    private final AtomicInteger unsolvedKind = new AtomicInteger();

    public UnsolvedType newUnsolvedType(Kind kind) {
        return new UnsolvedType(unsolvedType.getAndIncrement(), kind);
    }

    public UnsolvedType newUnsolvedType() {
        return new UnsolvedType(unsolvedType.getAndIncrement(), newUnsolvedKind());
    }

    public UnsolvedKind newUnsolvedKind() {
        return new UnsolvedKind(unsolvedKind.getAndIncrement());
    }

    public SyntheticVar newSyntheticVar() {
        return new SyntheticVar(unsolvedType.getAndIncrement(), newUnsolvedKind());
    }

    public SyntheticVar newSyntheticVar(Kind kind) {
        return new SyntheticVar(unsolvedType.getAndIncrement(), kind);
    }
}
