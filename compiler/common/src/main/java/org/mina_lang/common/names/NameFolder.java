/*
 * SPDX-FileCopyrightText:  © 2025 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.common.names;

public interface NameFolder<A> {
    default A visitName(Name name) {
        return name.accept(this);
    }

    A visitNameless(Nameless nameless);

    default A visitNamed(Named named) {
        return named.accept(this);
    }

    A visitBuiltInName(BuiltInName builtIn);

    A visitNamespaceName(NamespaceName namespace);

    A visitLocalName(LocalName local);

    A visitFieldName(FieldName field);

    A visitSyntheticName(SyntheticName synthetic);

    default A visitDeclarationName(DeclarationName declaration) {
        return declaration.accept(this);
    }

    A visitLetName(LetName let);

    default A visitTypeName(TypeName typ) {
        return typ.accept(this);
    }

    A visitConstructorName(ConstructorName constructor);

    A visitDataName(DataName data);

    default A visitTypeVarName(TypeVarName typeVar) {
        return typeVar.accept(this);
    }

    A visitExistsVarName(ExistsVarName existsVar);

    A visitForAllVarName(ForAllVarName forAllVar);
}
