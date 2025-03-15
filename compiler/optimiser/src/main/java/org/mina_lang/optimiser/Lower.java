/*
 * SPDX-FileCopyrightText:  © 2025 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.optimiser;

import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.factory.Lists;
import org.mina_lang.common.Attributes;
import org.mina_lang.common.names.*;
import org.mina_lang.common.types.*;
import org.mina_lang.ina.*;
import org.mina_lang.ina.Boolean;
import org.mina_lang.ina.Double;
import org.mina_lang.ina.Float;
import org.mina_lang.ina.Long;
import org.mina_lang.ina.String;
import org.mina_lang.syntax.*;

import java.io.IOException;
import java.util.List;

public class Lower {
    private final SyntheticNameSupply nameSupply;
    private final MetaNodePrinter<Attributes> nodePrinter = new MetaNodePrinter<>();
    private final InaNodePrinter printer = new InaNodePrinter();

    public Lower(SyntheticNameSupply nameSupply) {
        this.nameSupply = nameSupply;
    }

    public Type getUnderlyingType(org.mina_lang.common.types.Type type) {
        while (type instanceof QuantifiedType quant) {
            type = quant.body();
        }
        return type;
    }

    public Type getUnderlyingType(MetaNode<Attributes> node) {
        return getUnderlyingType((Type) node.meta().meta().sort());
    }

    public Namespace lower(NamespaceNode<Attributes> namespace) {
        var name = (NamespaceName) namespace.meta().meta().name();
        var declarations = namespace.declarationGroups().flatCollect(this::lowerDeclarationGroup);
        return new Namespace(name, declarations);
    }

    ImmutableList<Declaration> lowerDeclarationGroup(ImmutableList<DeclarationNode<Attributes>> declarations) {
        return declarations.collect(this::lowerDeclaration);
    }

    Declaration lowerDeclaration(DeclarationNode<Attributes> declaration) {
        if (declaration instanceof DataNode<Attributes> data) {
            return lowerData(data);
        } else if (declaration instanceof LetNode<Attributes> let) {
            return lowerLet(let);
        } else if (declaration instanceof LetFnNode<Attributes> letFn) {
            return lowerLetFn(letFn);
        }

        return null;
    }

    Data lowerData(DataNode<Attributes> data) {
        var name = (DataName) data.meta().meta().name();

        ImmutableList<TypeVar> typeParams = data.typeParams().collect(tyVar -> {
            var kind = (Kind) tyVar.meta().meta().sort();

            if (tyVar instanceof ForAllVarNode<Attributes> forall) {
                return new ForAllVar(forall.name(), kind);
            } else if (tyVar instanceof ExistsVarNode<Attributes> exists) {
                return new ExistsVar(exists.name(), kind);
            }

            return null;
        });

        var constructors = data.constructors().collect(this::lowerConstructor);

        return new Data(name, typeParams, constructors);
    }

    Constructor lowerConstructor(ConstructorNode<Attributes> constr) {
        var name = (ConstructorName) constr.meta().meta().name();
        var fields = constr.params().collect(this::lowerField);
        return new Constructor(name, fields);
    }

    Field lowerField(ConstructorParamNode<Attributes> param) {
        var name = (FieldName) param.meta().meta().name();
        var type = (Type) param.meta().meta().sort();
        return new Field(name, type);
    }

    Let lowerLet(LetNode<Attributes> let) {
        var name = (LetName) let.meta().meta().name();
        var type = (Type) let.meta().meta().sort();
        MutableList<LocalBinding> bindings = Lists.mutable.empty();
        var body = lowerExpr(let.expr(), bindings);
        return new Let(
            name, type,
            bindings.isEmpty() ? body : new Block(body.type(), bindings.toImmutableList(), body));
    }

    Let lowerLetFn(LetFnNode<Attributes> letFn) {
        var name = (LetName) letFn.meta().meta().name();
        var type = (Type) letFn.meta().meta().sort();
        var params = letFn.valueParams().collect(this::lowerParam);
        MutableList<LocalBinding> bindings = Lists.mutable.empty();
        var body = lowerExpr(letFn.expr(), bindings);
        return new Let(
            name, type,
            new Lambda(type, params,
                bindings.isEmpty() ? body : new Block(body.type(), bindings.toImmutableList(), body)));
    }

    Param lowerParam(ParamNode<Attributes> param) {
        var name = (LocalName) param.meta().meta().name();
        var type = (Type) param.meta().meta().sort();
        return new Param(name, type);
    }

    Value lowerToValue(List<LocalBinding> bindings, ExprNode<Attributes> expr) {
        var loweredExpr = lowerExpr(expr, bindings);

        Value value;
        if (loweredExpr instanceof Value loweredValue) {
            value = loweredValue;
        } else {
            var exprName = nameSupply.newSyntheticName();
            var exprType = (Type) expr.meta().meta().sort();
            bindings.add(new LetAssign(exprName, exprType, loweredExpr));
            value = new Reference(exprName, exprType);
        }

        return value;
    }

    Expression lowerExpr(ExprNode<Attributes> expr, List<LocalBinding> bindings) {
        if (expr instanceof ApplyNode<Attributes> apply) {
            return lowerApply(apply, bindings);
        } else if (expr instanceof SelectNode<Attributes> select) {
            return lowerSelect(select);
        } else if (expr instanceof BlockNode<Attributes> block) {
            return lowerBlock(block, bindings);
        } else if (expr instanceof IfNode<Attributes> ifExpr) {
            return lowerIf(ifExpr, bindings);
        } else if (expr instanceof LambdaNode<Attributes> lambda) {
            return lowerLambda(lambda);
        } else if (expr instanceof MatchNode<Attributes> match) {
            return lowerMatch(match, bindings);
        } else if (expr instanceof UnaryOpNode<Attributes> unOp) {
            return lowerUnaryOp(unOp, bindings);
        } else if (expr instanceof BinaryOpNode<Attributes> binOp) {
            return lowerBinaryOp(binOp, bindings);
        } else if (expr instanceof ReferenceNode<Attributes> reference) {
            return lowerReference(reference);
        } else if (expr instanceof LiteralNode<Attributes> literal) {
            return lowerLiteral(literal);
        }

        return null;
    }

    Expression lowerApply(ApplyNode<Attributes> apply, List<LocalBinding> bindings) {
        var applyType = (Type) apply.meta().meta().sort();

        // Eliminate selections that are applied immediately, replacing them with
        // a function call with the receiver as the first argument
        ExprNode<Attributes> appliedExpr;
        ImmutableList<ExprNode<Attributes>> appliedArgs;
        if (apply.expr() instanceof SelectNode<Attributes> select) {
            appliedExpr = select.selection();
            appliedArgs = Lists.immutable.of(select.receiver()).newWithAll(apply.args());
        } else {
            appliedExpr = apply.expr();
            appliedArgs = apply.args();
        }

        var funType = (TypeApply) getUnderlyingType(appliedExpr);
        var returnType = funType.typeArguments().getLast();

        var loweredFn = lowerToValue(bindings, appliedExpr);

        var args = appliedArgs
            .zip(funType.typeArguments().take(funType.typeArguments().size() - 1))
            .collect(pair -> {
                var loweredArg = lowerToValue(bindings, pair.getOne());
                var funArgType = pair.getTwo();
                // Box primitive arguments to polymorphic functions
                if (loweredArg.type().isPrimitive() && !funArgType.isPrimitive()) {
                    return new Box(loweredArg);
                } else {
                    return loweredArg;
                }
            });

        var loweredApply = new Apply(applyType, loweredFn, args);

        Expression tailExpr;

        // Unbox primitive return values of polymorphic functions
        if (applyType.isPrimitive() && !returnType.isPrimitive()) {
            var applyName = nameSupply.newSyntheticName();
            bindings.add(new LetAssign(applyName, applyType, loweredApply));
            tailExpr = new Unbox(new Reference(applyName, applyType));
        } else {
            tailExpr = loweredApply;
        }

        return tailExpr;
    }

    Expression lowerBlock(BlockNode<Attributes> block, List<LocalBinding> bindings) {
        // Flatten nested blocks into the enclosing scope
        block.declarations().forEach(let -> {
            var letName = (LocalName) let.meta().meta().name();
            var letType = (Type) let.meta().meta().sort();
            var letExpr = lowerExpr(let.expr(), bindings);
            bindings.add(new LetAssign(letName, letType, letExpr));
        });

        return block.result()
            .map(result -> lowerExpr(result, bindings))
            .orElse(Unit.INSTANCE);
    }

    Expression lowerIf(IfNode<Attributes> ifExpr, List<LocalBinding> bindings) {
        var type = (Type) ifExpr.meta().meta().sort();

        var condition = lowerToValue(bindings, ifExpr.condition());

        MutableList<LocalBinding> consequentBindings = Lists.mutable.empty();
        var consequent = lowerExpr(ifExpr.consequent(), consequentBindings);

        MutableList<LocalBinding> alternativeBindings = Lists.mutable.empty();
        var alternative = lowerExpr(ifExpr.alternative(), alternativeBindings);

        return new If(
            type, condition,
            consequentBindings.isEmpty()
                ? consequent
                : new Block(consequent.type(), consequentBindings.toImmutableList(), consequent),
            alternativeBindings.isEmpty()
                ? alternative
                : new Block(alternative.type(), alternativeBindings.toImmutableList(), alternative));
    }

    Expression lowerSelect(SelectNode<Attributes> select) {
        // Eliminate selections that aren't applied immediately:
        // To do this, we create a lambda to represent the partial application of the selection
        // receiver.selection ==> (..restArgs) => selection(receiver, ..restArgs)
        var type = (Type) select.meta().meta().sort();
        var adaptedFunType = (TypeApply) getUnderlyingType(type);
        var adaptedReturnType = adaptedFunType.typeArguments().getLast();

        // The params of the lambda are new synthetic names
        var params = adaptedFunType.typeArguments()
            .take(adaptedFunType.typeArguments().size() - 1)
            .collect((paramTy) -> new Param(nameSupply.newSyntheticName(), paramTy));

        // The body of the lambda is an application of the selection to the lowered receiver and any residual args
        MutableList<LocalBinding> bindings = Lists.mutable.empty();
        var selection = lowerReference(select.selection());

        var receiver = lowerToValue(bindings, select.receiver());

        // The underlying (potentially polymorphic) function type
        var selectionType = (Type) select.selection().meta().meta().sort();
        var underlyingFunType = (TypeApply) getUnderlyingType(selectionType);
        var underlyingReturnType = underlyingFunType.typeArguments().getLast();

        // Assemble the full argument list of the underlying function
        var residualArgs = params.collect(param -> new Reference(param.name(), param.type()));
        var appliedArgs = Lists.immutable.of(receiver).newWithAll(residualArgs);

        var args = appliedArgs
            .zip(underlyingFunType.typeArguments().take(underlyingFunType.typeArguments().size() - 1))
            .collect(pair -> {
                var loweredArg = pair.getOne();
                var underlyingArgType = pair.getTwo();
                // Box primitive arguments to polymorphic functions
                if (loweredArg.type().isPrimitive() && !underlyingArgType.isPrimitive()) {
                    return new Box(loweredArg);
                } else {
                    return loweredArg;
                }
            });

        var loweredApply = new Apply(adaptedReturnType, selection, args);

        Expression tailExpr;

        // Unbox primitive return values of polymorphic functions
        if (adaptedReturnType.isPrimitive() && !underlyingReturnType.isPrimitive()) {
            var applyName = nameSupply.newSyntheticName();
            bindings.add(new LetAssign(applyName, adaptedReturnType, loweredApply));
            tailExpr = new Unbox(new Reference(applyName, adaptedReturnType));
        } else {
            tailExpr = loweredApply;
        }

        var body = bindings.isEmpty() ? tailExpr : new Block(adaptedReturnType, bindings.toImmutableList(), tailExpr);

        return new Lambda(type, params, body);
    }

    Expression lowerLambda(LambdaNode<Attributes> lambda) {
        var type = (Type) lambda.meta().meta().sort();
        var params = lambda.params().collect(this::lowerParam);
        MutableList<LocalBinding> bindings = Lists.mutable.empty();
        var body = lowerExpr(lambda.body(), bindings);
        return new Lambda(
            type, params,
            bindings.isEmpty() ? body : new Block(body.type(), bindings.toImmutableList(), body));
    }

    Expression lowerMatch(MatchNode<Attributes> match, List<LocalBinding> bindings) {
        var type = (Type) match.meta().meta().sort();
        var scrutinee = lowerToValue(bindings, match.scrutinee());
        var cases = match.cases().collect(this::lowerCase);
        return new Match(type, scrutinee, cases);
    }

    Case lowerCase(CaseNode<Attributes> cse) {
        var pattern = lowerPattern(cse.pattern());
        MutableList<LocalBinding> bindings = Lists.mutable.empty();
        var consequent = lowerExpr(cse.consequent(), bindings);
        return new Case(
            pattern,
            bindings.isEmpty()
                ? consequent
                : new Block(consequent.type(), bindings.toImmutableList(), consequent));
    }

    Pattern lowerPattern(PatternNode<Attributes> pattern) {
        if (pattern instanceof LiteralPatternNode<Attributes> lit) {
            var literal = lowerLiteral(lit.literal());
            return new LiteralPattern(literal);
        } else if (pattern instanceof IdPatternNode<Attributes> id) {
            var name = (LocalName) id.meta().meta().name();
            var type = (Type) id.meta().meta().sort();
            return new IdPattern(name, type);
        } else if (pattern instanceof AliasPatternNode<Attributes> alias) {
            var name = (LocalName) alias.meta().meta().name();
            var type = (Type) alias.meta().meta().sort();
            var aliased = lowerPattern(alias.pattern());
            return new AliasPattern(name, type, aliased);
        } else if (pattern instanceof ConstructorPatternNode<Attributes> constr) {
            var name = (ConstructorName) constr.meta().meta().name();
            var type = (Type) constr.meta().meta().sort();
            var fields = constr.fields().collect(field -> lowerFieldPattern(name, field));
            return new ConstructorPattern(name, type, fields);
        }

        return null;
    }

    // TODO: Think about desugaring field pattern to IdPatternNode earlier to avoid this wrangling
    FieldPattern lowerFieldPattern(ConstructorName constrName, FieldPatternNode<Attributes> field) {
        var type = (Type) field.meta().meta().sort();
        var pattern = field.pattern().map(this::lowerPattern).orElseGet(() -> {
            var name = (LocalName) field.meta().meta().name();
            return new IdPattern(name, type);
        });
        return new FieldPattern(new FieldName(constrName, field.field()), type, pattern);
    }

    Expression lowerUnaryOp(UnaryOpNode<Attributes> unOp, List<LocalBinding> bindings) {
        var type = (Type) unOp.meta().meta().sort();
        var operand = lowerToValue(bindings, unOp.operand());
        return new UnOp(type, unOp.operator(), operand);
    }

    Expression lowerBinaryOp(BinaryOpNode<Attributes> binOp, List<LocalBinding> bindings) {
        var type = (Type) binOp.meta().meta().sort();
        var left = lowerToValue(bindings, binOp.leftOperand());
        var right = lowerToValue(bindings, binOp.rightOperand());
        return new BinOp(type, left, binOp.operator(), right);
    }

    Reference lowerReference(ReferenceNode<Attributes> reference) {
        var name = (ValueName) reference.meta().meta().name();
        var type = (Type) reference.meta().meta().sort();
        return new Reference(name, type);
    }

    Literal lowerLiteral(LiteralNode<Attributes> literal) {
        if (literal instanceof BooleanNode<Attributes> bool) {
            return new Boolean(bool.value());
        } else if (literal instanceof CharNode<Attributes> chr) {
            return new Char(chr.value());
        } else if (literal instanceof IntNode<Attributes> intgr) {
            return new Int(intgr.value());
        } else if (literal instanceof LongNode<Attributes> lng) {
            return new Long(lng.value());
        } else if (literal instanceof FloatNode<Attributes> flt) {
            return new Float(flt.value());
        } else if (literal instanceof DoubleNode<Attributes> dbl) {
            return new Double(dbl.value());
        } else if (literal instanceof StringNode<Attributes> string) {
            return new String(string.value());
        }

        return null;
    }
}
