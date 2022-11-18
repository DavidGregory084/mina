package org.mina_lang.common.types;

import java.util.concurrent.atomic.AtomicInteger;

public class UnsolvedVariableSupply {
    private final AtomicInteger unsolvedType = new AtomicInteger();
    private final AtomicInteger unsolvedKind = new AtomicInteger();

    public UnsolvedType newUnsolvedType() {
        return new UnsolvedType(unsolvedType.getAndIncrement(), newUnsolvedKind());
    }

    public UnsolvedKind newUnsolvedKind() {
        return new UnsolvedKind(unsolvedKind.getAndIncrement());
    }
}
