/*
 * SPDX-FileCopyrightText:  © 2026 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.optimiser.constants;

import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.set.ImmutableSet;
import org.eclipse.collections.api.set.MutableSet;
import org.eclipse.collections.impl.factory.Sets;
import org.mina_lang.common.names.*;
import org.mina_lang.common.operators.BinaryOp;
import org.mina_lang.common.operators.UnaryOp;
import org.mina_lang.common.types.QuantifiedType;
import org.mina_lang.common.types.Type;
import org.mina_lang.common.types.TypeVar;
import org.mina_lang.ina.InaNodeFolder;

public class FreeVariablesFolder implements InaNodeFolder<ImmutableSet<Named>> {
    MutableSet<Name> boundVariables = Sets.mutable.empty();

    private Type getUnderlyingType(Type type) {
        while (type instanceof QuantifiedType quant) {
            type = quant.body();
        }
        return type;
    }

    @Override
    public ImmutableSet<Named> visitNamespace(NamespaceName name, ImmutableList<ImmutableSet<Named>> declarations) {
        return declarations.stream().reduce(Sets.immutable.empty(), ImmutableSet::union);
    }

    @Override
    public ImmutableSet<Named> visitData(DataName name, ImmutableList<TypeVar> typeParams, ImmutableList<ImmutableSet<Named>> constructors) {
        return Sets.immutable.empty();
    }

    @Override
    public ImmutableSet<Named> visitConstructor(ConstructorName name, ImmutableList<ImmutableSet<Named>> fields) {
        return Sets.immutable.empty();
    }

    @Override
    public ImmutableSet<Named> visitField(FieldName name, Type type) {
        return Sets.immutable.empty();
    }

    @Override
    public ImmutableSet<Named> visitLet(LetName name, Type type, ImmutableSet<Named> body) {
        return Type.isFunction(getUnderlyingType(type)) ? body.newWith(name) : body;
    }

    @Override
    public ImmutableSet<Named> visitParam(LocalBindingName name, Type type) {
        return Sets.immutable.empty();
    }

    @Override
    public ImmutableSet<Named> visitLetAssign(LocalBindingName name, Type type, ImmutableSet<Named> body) {
        return body;
    }

    @Override
    public ImmutableSet<Named> visitJoin(LocalBindingName name, Type type, ImmutableList<ImmutableSet<Named>> params, ImmutableSet<Named> body) {
        return body;
    }

    @Override
    public ImmutableSet<Named> visitApply(Type type, ImmutableSet<Named> expr, ImmutableList<ImmutableSet<Named>> args) {
        return expr.newWithAll(args.flatCollect(it -> it));
    }

    @Override
    public ImmutableSet<Named> visitBinOp(Type type, ImmutableSet<Named> left, BinaryOp operator, ImmutableSet<Named> right) {
        return left.union(right);
    }

    @Override
    public ImmutableSet<Named> visitBlock(Type type, ImmutableList<ImmutableSet<Named>> bindings, ImmutableSet<Named> result) {
        return result
            .newWithAll(bindings.flatCollect(it -> it))
            .reject(boundVariables::contains); // Ignore variables declared inside this block
    }

    @Override
    public ImmutableSet<Named> visitIf(Type type, ImmutableSet<Named> cond, ImmutableSet<Named> consequent, ImmutableSet<Named> alternative) {
        return cond.union(consequent).union(alternative);
    }

    @Override
    public ImmutableSet<Named> visitLambda(Type type, ImmutableList<ImmutableSet<Named>> params, ImmutableSet<Named> body) {
        return body;
    }

    @Override
    public ImmutableSet<Named> visitReference(ValueName name, Type type) {
        return Sets.immutable.of(name);
    }

    @Override
    public ImmutableSet<Named> visitUnOp(Type type, UnaryOp operator, ImmutableSet<Named> operand) {
        return operand;
    }

    @Override
    public ImmutableSet<Named> visitMatch(Type type, ImmutableSet<Named> scrutinee, ImmutableList<ImmutableSet<Named>> cases) {
        return scrutinee
            .newWithAll(cases.flatCollect(it -> it))
            .reject(boundVariables::contains); // Ignore pattern variables declared inside this match statement
    }

    @Override
    public ImmutableSet<Named> visitCase(ImmutableSet<Named> pattern, ImmutableSet<Named> consequent) {
        return consequent;
    }

    @Override
    public ImmutableSet<Named> visitAliasPattern(LocalName alias, Type type, ImmutableSet<Named> pattern) {
        boundVariables.add(alias);
        return Sets.immutable.empty();
    }

    @Override
    public ImmutableSet<Named> visitConstructorPattern(ConstructorName name, Type type, ImmutableList<ImmutableSet<Named>> fields) {
        return Sets.immutable.empty();
    }

    @Override
    public ImmutableSet<Named> visitFieldPattern(FieldName name, Type type, ImmutableSet<Named> pattern) {
        return Sets.immutable.empty();
    }

    @Override
    public ImmutableSet<Named> visitIdPattern(LocalName name, Type type) {
        boundVariables.add(name);
        return Sets.immutable.empty();
    }

    @Override
    public ImmutableSet<Named> visitLiteralPattern(ImmutableSet<Named> literal) {
        return Sets.immutable.empty();
    }

    @Override
    public ImmutableSet<Named> visitBoolean(boolean value) {
        return Sets.immutable.empty();
    }

    @Override
    public ImmutableSet<Named> visitChar(char value) {
        return Sets.immutable.empty();
    }

    @Override
    public ImmutableSet<Named> visitInt(int value) {
        return Sets.immutable.empty();
    }

    @Override
    public ImmutableSet<Named> visitLong(long value) {
        return Sets.immutable.empty();
    }

    @Override
    public ImmutableSet<Named> visitFloat(float value) {
        return Sets.immutable.empty();
    }

    @Override
    public ImmutableSet<Named> visitDouble(double value) {
        return Sets.immutable.empty();
    }

    @Override
    public ImmutableSet<Named> visitString(String value) {
        return Sets.immutable.empty();
    }

    @Override
    public ImmutableSet<Named> visitUnit() {
        return Sets.immutable.empty();
    }

    @Override
    public ImmutableSet<Named> visitBox(ImmutableSet<Named> value) {
        return value;
    }

    @Override
    public ImmutableSet<Named> visitUnbox(ImmutableSet<Named> value) {
        return value;
    }
}
