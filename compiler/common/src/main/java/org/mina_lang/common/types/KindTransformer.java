package org.mina_lang.common.types;

public interface KindTransformer {
    default Kind visitKind(Kind kind) {
        return kind.accept(this);
    }

    TypeKind visitTypeKind(TypeKind typ);

    Kind visitUnsolvedKind(UnsolvedKind unsolved);

    HigherKind visitHigherKind(HigherKind higher);
}
