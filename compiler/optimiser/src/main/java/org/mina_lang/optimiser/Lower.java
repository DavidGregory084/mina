/*
 * SPDX-FileCopyrightText:  © 2025 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.optimiser;

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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

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

    public Value boxArgValue(Value loweredArg, Type argType) {
        // Box primitive arguments to polymorphic functions
        if (loweredArg.type().isPrimitive() && !argType.isPrimitive()) {
            return new Box(loweredArg);
        } else {
            return loweredArg;
        }
    }

    public Expression unboxReturnValue(List<LocalBinding> bindings, Apply loweredApply, Type applyType, Type returnType) {
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

    public Namespace lower(NamespaceNode<Attributes> namespace) {
        var name = (NamespaceName) namespace.meta().meta().name();
        var declarations = namespace.declarationGroups().stream()
            .flatMap(grp -> lowerDeclarationGroup(grp).stream());
        return new Namespace(name, declarations.toList());
    }

    List<Declaration> lowerDeclarationGroup(List<DeclarationNode<Attributes>> declarations) {
        return declarations.stream().map(this::lowerDeclaration).toList();
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

        var typeParams = data.typeParams().stream().<TypeVar>map(tyVar -> {
            var kind = (Kind) tyVar.meta().meta().sort();

            if (tyVar instanceof ForAllVarNode<Attributes> forall) {
                return new ForAllVar(forall.name(), kind);
            } else if (tyVar instanceof ExistsVarNode<Attributes> exists) {
                return new ExistsVar(exists.name(), kind);
            }

            return null;
        });

        var constructors = data.constructors().stream().map(this::lowerConstructor);

        return new Data(name, typeParams.toList(), constructors.toList());
    }

    Constructor lowerConstructor(ConstructorNode<Attributes> constr) {
        var name = (ConstructorName) constr.meta().meta().name();
        var fields = constr.params().stream().map(this::lowerField);
        return new Constructor(name, fields.toList());
    }

    Field lowerField(ConstructorParamNode<Attributes> param) {
        var name = (FieldName) param.meta().meta().name();
        var type = (Type) param.meta().meta().sort();
        return new Field(name, type);
    }

    Let lowerLet(LetNode<Attributes> let) {
        var name = (LetName) let.meta().meta().name();
        var type = (Type) let.meta().meta().sort();
        List<LocalBinding> bindings = new ArrayList<>();
        var body = lowerExpr(let.expr(), bindings);
        return new Let(
            name, type,
            bindings.isEmpty() ? body : new Block(body.type(), bindings, body));
    }

    Let lowerLetFn(LetFnNode<Attributes> letFn) {
        var name = (LetName) letFn.meta().meta().name();
        var type = (Type) letFn.meta().meta().sort();
        var params = letFn.valueParams().stream().map(this::lowerParam);
        List<LocalBinding> bindings = new ArrayList<>();
        var body = lowerExpr(letFn.expr(), bindings);
        return new Let(
            name, type,
            new Lambda(type, params.toList(),
                bindings.isEmpty() ? body : new Block(body.type(), bindings, body)));
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
        List<ExprNode<Attributes>> appliedArgs;
        if (apply.expr() instanceof SelectNode<Attributes> select) {
            appliedExpr = select.selection();
            appliedArgs = new ArrayList<>();
            appliedArgs.add(select.receiver());
            appliedArgs.addAll(apply.args());
        } else {
            appliedExpr = apply.expr();
            appliedArgs = apply.args();
        }

        var funType = (TypeApply) getUnderlyingType(appliedExpr);
        var returnType = funType.typeArguments().get(funType.typeArguments().size() - 1);

        var loweredFn = lowerToValue(bindings, appliedExpr);

        var args = IntStream.range(0, Math.min(appliedArgs.size(), funType.typeArguments().size()))
            .mapToObj(index -> {
                var loweredArg = lowerToValue(bindings, appliedArgs.get(index));
                return boxArgValue(loweredArg, funType.typeArguments().get(index));
            });

        var loweredApply = new Apply(applyType, loweredFn, args.toList());

        return unboxReturnValue(bindings, loweredApply, applyType, returnType);
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

        List<LocalBinding> consequentBindings = new ArrayList<>();
        var consequent = lowerExpr(ifExpr.consequent(), consequentBindings);

        List<LocalBinding> alternativeBindings = new ArrayList<>();
        var alternative = lowerExpr(ifExpr.alternative(), alternativeBindings);

        return new If(
            type, condition,
            consequentBindings.isEmpty()
                ? consequent
                : new Block(consequent.type(), consequentBindings, consequent),
            alternativeBindings.isEmpty()
                ? alternative
                : new Block(alternative.type(), alternativeBindings, alternative));
    }

    Expression lowerSelect(SelectNode<Attributes> select) {
        // Eliminate selections that aren't applied immediately:
        // To do this, we create a lambda to represent the partial application of the selection
        // receiver.selection ==> (..restArgs) => selection(receiver, ..restArgs)
        var type = (Type) select.meta().meta().sort();
        var adaptedFunType = (TypeApply) getUnderlyingType(type);
        var adaptedReturnType = adaptedFunType.typeArguments().get(adaptedFunType.typeArguments().size() - 1);

        // The params of the lambda are new synthetic names
        var params = adaptedFunType.typeArguments()
            .subList(0, adaptedFunType.typeArguments().size() - 1)
            .stream().map((paramTy) -> new Param(nameSupply.newSyntheticName(), paramTy))
            .toList();

        // The body of the lambda is an application of the selection to the lowered receiver and any residual args
        List<LocalBinding> bindings = new ArrayList<>();
        var selection = lowerReference(select.selection());

        var receiver = lowerToValue(bindings, select.receiver());

        // The underlying (potentially polymorphic) function type
        var selectionType = (Type) select.selection().meta().meta().sort();
        var underlyingFunType = (TypeApply) getUnderlyingType(selectionType);
        var underlyingReturnType = underlyingFunType.typeArguments().get(underlyingFunType.typeArguments().size() - 1);

        // Assemble the full argument list of the underlying function
        var residualArgs = params.stream().map(param -> new Reference(param.name(), param.type()));
        var appliedArgs = new ArrayList<Value>();
        appliedArgs.add(receiver);
        appliedArgs.addAll(residualArgs.toList());

        var args = IntStream.range(0, Math.min(appliedArgs.size(), underlyingFunType.typeArguments().size()))
            .mapToObj(index -> {
                var loweredArg = appliedArgs.get(index);
                return boxArgValue(loweredArg, underlyingFunType.typeArguments().get(index));
            });

        var loweredApply = new Apply(adaptedReturnType, selection, args.toList());

        var tailExpr = unboxReturnValue(bindings, loweredApply, adaptedReturnType, underlyingReturnType);

        var body = bindings.isEmpty() ? tailExpr : new Block(adaptedReturnType, bindings, tailExpr);

        return new Lambda(type, params, body);
    }

    Expression lowerLambda(LambdaNode<Attributes> lambda) {
        var type = (Type) lambda.meta().meta().sort();
        var params = lambda.params().stream().map(this::lowerParam);
        List<LocalBinding> bindings = new ArrayList<>();
        var body = lowerExpr(lambda.body(), bindings);
        return new Lambda(
            type, params.toList(),
            bindings.isEmpty() ? body : new Block(body.type(), bindings, body));
    }

    Expression lowerMatch(MatchNode<Attributes> match, List<LocalBinding> bindings) {
        var type = (Type) match.meta().meta().sort();
        var scrutinee = lowerToValue(bindings, match.scrutinee());
        var cases = match.cases().stream().map(this::lowerCase);
        return new Match(type, scrutinee, cases.toList());
    }

    Case lowerCase(CaseNode<Attributes> cse) {
        var pattern = lowerPattern(cse.pattern());
        List<LocalBinding> bindings = new ArrayList<>();
        var consequent = lowerExpr(cse.consequent(), bindings);
        return new Case(
            pattern,
            bindings.isEmpty()
                ? consequent
                : new Block(consequent.type(), bindings, consequent));
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
            var fields = constr.fields().stream().map(field -> lowerFieldPattern(name, field));
            return new ConstructorPattern(name, type, fields.toList());
        }

        return null;
    }

    FieldPattern lowerFieldPattern(ConstructorName constrName, FieldPatternNode<Attributes> field) {
        var name = (FieldName) field.meta().meta().name();
        var type = (Type) field.meta().meta().sort();
        var pattern = lowerPattern(field.pattern());
        return new FieldPattern(name, type, pattern);
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
