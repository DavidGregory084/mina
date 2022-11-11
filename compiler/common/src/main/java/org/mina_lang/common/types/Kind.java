package org.mina_lang.common.types;

public sealed interface Kind extends Sort permits TypeKind, HigherKind, UnsolvedKind {

}
