package org.mina_lang.common.types;

import java.util.Map;

public class KindDefaultingTransformer extends KindSubstitutionTransformer {

    public KindDefaultingTransformer() {
        super();
    }

    public KindDefaultingTransformer(Map<UnsolvedKind, Kind> substitution) {
        super(substitution);
    }

    @Override
    public Kind visitUnsolvedKind(UnsolvedKind unsolved) {
        return substitution.getOrDefault(unsolved, TypeKind.INSTANCE);
    }
}
