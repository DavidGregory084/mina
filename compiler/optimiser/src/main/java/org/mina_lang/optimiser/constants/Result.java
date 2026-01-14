package org.mina_lang.optimiser.constants;

public sealed interface Result permits Constant, NonConstant, Unassigned {
    float compare(Result other);

    static Result leastUpperBound(Result l, Result r) {
        var comparison = l.compare(r);
        if (Float.isNaN(comparison)) {
            // Conflicting constant values
            return NonConstant.VALUE;
        } else if (comparison < 0.0F) {
            // l < r
            return r;
        } else {
            // l > r or l == r
            return l;
        }
    }
}
