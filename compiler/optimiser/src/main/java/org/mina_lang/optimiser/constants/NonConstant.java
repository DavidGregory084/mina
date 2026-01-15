package org.mina_lang.optimiser.constants;

public enum NonConstant implements Result {
    VALUE;

    @Override
    public float compare(Result other) {
        return other == NonConstant.VALUE ? 0.0F : 1.0F;
    }

    @Override
    public String toString() {
        return "NonConstant";
    }
}
