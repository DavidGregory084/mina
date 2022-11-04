package org.mina_lang.common.types;

public enum Polarity {
    POSITIVE,
    NEGATIVE,
    NON_POLAR;

    public boolean nonNegative() {
        return this != NEGATIVE;
    }

    public boolean nonPositive() {
        return this != POSITIVE;
    }

    public static Polarity join(Polarity left, Polarity right) {
        return switch (left) {
            case POSITIVE, NEGATIVE -> left;
            case NON_POLAR ->
                switch (right) {
                    case POSITIVE, NEGATIVE -> right;
                    case NON_POLAR -> NEGATIVE;
                };
        };
    }
}
