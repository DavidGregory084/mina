/*
 * SPDX-FileCopyrightText:  © 2025 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.ina;

import org.eclipse.collections.api.list.ImmutableList;
import org.mina_lang.common.names.*;
import org.mina_lang.common.operators.BinaryOp;
import org.mina_lang.common.operators.UnaryOp;
import org.mina_lang.common.types.Type;
import org.mina_lang.common.types.TypeVar;

public interface InaNodeFolder<A> {
    // Namespaces
    A visitNamespace(NamespaceName name, ImmutableList<A> declarations);

    // Declarations
    default A visitDeclaration(Declaration decl)  {
        return decl.accept(this);
    }

    A visitData(DataName name, ImmutableList<TypeVar> typeParams, ImmutableList<A> constructors);

    A visitConstructor(ConstructorName name, ImmutableList<A> fields);

    A visitField(FieldName name, Type type);

    A visitLet(LetName name, Type type, A body);

    // Params
    A visitParam(LocalBindingName name, Type type);

    // Local bindings
    default A visitLocalBinding(LocalBinding local) {
        return local.accept(this);
    }

    A visitLetAssign(LocalBindingName name, Type type, A body);

    A visitJoin(LocalBindingName name, Type type, ImmutableList<A> params, A body);

    // Expressions
    default A visitExpr(Expression expr) {
        return expr.accept(this);
    }

    A visitApply(Type type, A expr, ImmutableList<A> args);

    A visitBinOp(Type type, A left, BinaryOp operator, A right);

    A visitBlock(Type type, ImmutableList<A> bindings, A result);

    A visitIf(Type type, A cond, A consequent, A alternative);

    A visitLambda(Type type, ImmutableList<A> params, A body);

    A visitReference(ValueName name, Type type);

    A visitUnOp(Type type, UnaryOp operator, A operand);

    A visitMatch(Type type, A scrutinee, ImmutableList<A> cases);

    A visitCase(A pattern, A consequent);

    // Patterns
    A visitAliasPattern(LocalName alias, Type type, A pattern);

    A visitConstructorPattern(ConstructorName name, Type type, ImmutableList<A> fields);

    A visitFieldPattern(FieldName name, Type type, A pattern);

    A visitIdPattern(LocalName name, Type type);

    A visitLiteralPattern(A literal);

    // Literals
    default A visitLiteral(Literal literal) {
        return literal.accept(this);
    }

    A visitBoolean(boolean value);

    A visitChar(char value);

    A visitInt(int value);

    A visitLong(long value);

    A visitFloat(float value);

    A visitDouble(double value);

    A visitString(java.lang.String value);

    A visitUnit();

    // Primitive boxing / unboxing
    A visitBox(A value);

    A visitUnbox(A value);
}
