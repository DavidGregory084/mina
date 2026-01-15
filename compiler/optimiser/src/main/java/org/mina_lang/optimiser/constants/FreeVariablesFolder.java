package org.mina_lang.optimiser.constants;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.set.MutableSet;
import org.eclipse.collections.impl.factory.Sets;
import org.mina_lang.common.names.*;
import org.mina_lang.common.operators.BinaryOp;
import org.mina_lang.common.operators.UnaryOp;
import org.mina_lang.common.types.Type;
import org.mina_lang.common.types.TypeVar;
import org.mina_lang.ina.InaNodeFolder;

public class FreeVariablesFolder implements InaNodeFolder<ImmutableList<Named>> {
    MutableSet<Named> boundVariables = Sets.mutable.empty();

    @Override
    public ImmutableList<Named> visitNamespace(NamespaceName name, ImmutableList<ImmutableList<Named>> declarations) {
        return Lists.immutable.empty();
    }

    @Override
    public ImmutableList<Named> visitData(DataName name, ImmutableList<TypeVar> typeParams, ImmutableList<ImmutableList<Named>> constructors) {
        return Lists.immutable.empty();
    }

    @Override
    public ImmutableList<Named> visitConstructor(ConstructorName name, ImmutableList<ImmutableList<Named>> fields) {
        return Lists.immutable.empty();
    }

    @Override
    public ImmutableList<Named> visitField(FieldName name, Type type) {
        return Lists.immutable.empty();
    }

    @Override
    public ImmutableList<Named> visitLet(LetName name, Type type, ImmutableList<Named> body) {
        return Type.isFunction(type) ? body.newWith(name) : body;
    }

    @Override
    public ImmutableList<Named> visitParam(LocalBindingName name, Type type) {
        return Lists.immutable.empty();
    }

    @Override
    public ImmutableList<Named> visitLetAssign(LocalBindingName name, Type type, ImmutableList<Named> body) {
        return body;
    }

    @Override
    public ImmutableList<Named> visitJoin(LocalBindingName name, Type type, ImmutableList<ImmutableList<Named>> params, ImmutableList<Named> body) {
        return body;
    }

    @Override
    public ImmutableList<Named> visitApply(Type type, ImmutableList<Named> expr, ImmutableList<ImmutableList<Named>> args) {
        return Lists.immutable.empty();
    }

    @Override
    public ImmutableList<Named> visitBinOp(Type type, ImmutableList<Named> left, BinaryOp operator, ImmutableList<Named> right) {
        return Lists.immutable.empty();
    }

    @Override
    public ImmutableList<Named> visitBlock(Type type, ImmutableList<ImmutableList<Named>> bindings, ImmutableList<Named> result) {
        return Lists.immutable.empty();
    }

    @Override
    public ImmutableList<Named> visitIf(Type type, ImmutableList<Named> cond, ImmutableList<Named> consequent, ImmutableList<Named> alternative) {
        return Lists.immutable.empty();
    }

    @Override
    public ImmutableList<Named> visitLambda(Type type, ImmutableList<ImmutableList<Named>> params, ImmutableList<Named> body) {
        return Lists.immutable.empty();
    }

    @Override
    public ImmutableList<Named> visitReference(ValueName name, Type type) {
        return Lists.immutable.empty();
    }

    @Override
    public ImmutableList<Named> visitUnOp(Type type, UnaryOp operator, ImmutableList<Named> operand) {
        return Lists.immutable.empty();
    }

    @Override
    public ImmutableList<Named> visitMatch(Type type, ImmutableList<Named> scrutinee, ImmutableList<ImmutableList<Named>> cases) {
        return Lists.immutable.empty();
    }

    @Override
    public ImmutableList<Named> visitCase(ImmutableList<Named> pattern, ImmutableList<Named> consequent) {
        return Lists.immutable.empty();
    }

    @Override
    public ImmutableList<Named> visitAliasPattern(LocalName alias, Type type, ImmutableList<Named> pattern) {
        return Lists.immutable.empty();
    }

    @Override
    public ImmutableList<Named> visitConstructorPattern(ConstructorName name, Type type, ImmutableList<ImmutableList<Named>> fields) {
        return Lists.immutable.empty();
    }

    @Override
    public ImmutableList<Named> visitFieldPattern(FieldName name, Type type, ImmutableList<Named> pattern) {
        return Lists.immutable.empty();
    }

    @Override
    public ImmutableList<Named> visitIdPattern(LocalName name, Type type) {
        return Lists.immutable.empty();
    }

    @Override
    public ImmutableList<Named> visitLiteralPattern(ImmutableList<Named> literal) {
        return Lists.immutable.empty();
    }

    @Override
    public ImmutableList<Named> visitBoolean(boolean value) {
        return Lists.immutable.empty();
    }

    @Override
    public ImmutableList<Named> visitChar(char value) {
        return Lists.immutable.empty();
    }

    @Override
    public ImmutableList<Named> visitInt(int value) {
        return Lists.immutable.empty();
    }

    @Override
    public ImmutableList<Named> visitLong(long value) {
        return Lists.immutable.empty();
    }

    @Override
    public ImmutableList<Named> visitFloat(float value) {
        return Lists.immutable.empty();
    }

    @Override
    public ImmutableList<Named> visitDouble(double value) {
        return Lists.immutable.empty();
    }

    @Override
    public ImmutableList<Named> visitString(String value) {
        return Lists.immutable.empty();
    }

    @Override
    public ImmutableList<Named> visitUnit() {
        return Lists.immutable.empty();
    }

    @Override
    public ImmutableList<Named> visitBox(ImmutableList<Named> value) {
        return value;
    }

    @Override
    public ImmutableList<Named> visitUnbox(ImmutableList<Named> value) {
        return value;
    }
}
