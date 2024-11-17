/*
 * SPDX-FileCopyrightText:  © 2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.common.names;

public interface NameVisitor {
    default void visitName(Name name) {
        name.accept(this);
    }

    void visitNameless(Nameless nameless);

    default void visitNamed(Named named) {
        named.accept(this);
    }

    void visitBuiltInName(BuiltInName builtIn);

    void visitNamespaceName(NamespaceName namespace);

    void visitLocalName(LocalName local);

    void visitFieldName(FieldName field);

    default void visitDeclarationName(DeclarationName declaration) {
        declaration.accept(this);
    }

    void visitLetName(LetName let);

    default void visitTypeName(TypeName typ) {
        typ.accept(this);
    }

    void visitConstructorName(ConstructorName constructor);

    void visitDataName(DataName data);

    default void visitTypeVarName(TypeVarName typeVar) {
        typeVar.accept(this);
    }

    void visitExistsVarName(ExistsVarName existsVar);

    void visitForAllVarName(ForAllVarName forAllVar);
}
