package org.mina_lang.common.types;

import org.eclipse.collections.api.factory.Maps;

import java.util.Map;

public class KindSubstitutionTransformer implements KindTransformer {

    protected Map<UnsolvedKind, Kind> substitution = Maps.mutable.empty();

    public KindSubstitutionTransformer() {
    }

    public KindSubstitutionTransformer(Map<UnsolvedKind, Kind> substitution) {
        this.substitution = substitution;
    }

    @Override
    public TypeKind visitTypeKind(TypeKind typ) {
        return typ;
    }

    @Override
    public Kind visitUnsolvedKind(UnsolvedKind unsolved) {
        return substitution.getOrDefault(unsolved, unsolved);
    }

    @Override
    public HigherKind visitHigherKind(HigherKind higher) {
        return new HigherKind(
            higher.argKinds().collect(argKind -> argKind.accept(this)),
            higher.resultKind().accept(this));
    }
}
