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
import org.mina_lang.common.types.QuantifiedType;
import org.mina_lang.common.types.Type;
import org.mina_lang.common.types.TypeApply;
import org.mina_lang.common.types.TypeVar;
import org.mina_lang.ina.*;
import org.mina_lang.ina.Boolean;
import org.mina_lang.ina.Double;
import org.mina_lang.ina.Float;
import org.mina_lang.ina.Long;
import org.mina_lang.ina.String;
import org.mina_lang.syntax.*;

import java.util.List;

public class Lower {
    private final SyntheticNameSupply nameSupply;

    public Lower(SyntheticNameSupply nameSupply) {
        this.nameSupply = nameSupply;
    }

    public Type getUnderlyingType(org.mina_lang.common.types.Type type) {
        while (type instanceof QuantifiedType quant) {
            type = quant.body();
        }
        return type;
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
        var typeParams = data.typeParams().collect(tyVar -> (TypeVar) tyVar.meta().meta().sort());
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
        var body = lowerExpr(let.expr());
        System.out.println(body);
        return new Let(name, type, body);
    }

    Let lowerLetFn(LetFnNode<Attributes> letFn) {
        var name = (LetName) letFn.meta().meta().name();
        var type = (Type) letFn.meta().meta().sort();
        var params = letFn.valueParams().collect(this::lowerParam);
        var body = lowerExpr(letFn.expr());
        System.out.println(body);
        return new Let(name, type, new Lambda(type, params, body));
    }

    Param lowerParam(ParamNode<Attributes> param) {
        var name = (LocalName) param.meta().meta().name();
        var type = (Type) param.meta().meta().sort();
        return new Param(name, type);
    }

    Value lowerToValue(List<LocalBinding> bindings, ExprNode<Attributes> expr) {
        var exprName = nameSupply.newSyntheticName();
        var exprType = (Type) expr.meta().meta().sort();
        var loweredExpr = lowerExpr(expr);

        Value value;
        if (loweredExpr instanceof Value loweredValue) {
            value = loweredValue;
        } else {
            bindings.add(new LetAssign(exprName, exprType, loweredExpr));
            value = new Reference(exprName, exprType);
        }

        return value;
    }

    Expression lowerExpr(ExprNode<Attributes> expr) {
        if (expr instanceof ApplyNode<Attributes> apply) {
            return lowerApply(apply);
        } else if (expr instanceof SelectNode<Attributes> select) {
            return lowerSelect(select);
        } else if (expr instanceof BlockNode<Attributes> block) {
            return lowerBlock(block);
        } else if (expr instanceof IfNode<Attributes> ifExpr) {
            return lowerIf(ifExpr);
        } else if (expr instanceof LambdaNode<Attributes> lambda) {
            return lowerLambda(lambda);
        } else if (expr instanceof MatchNode<Attributes> match) {
            return lowerMatch(match);
        } else if (expr instanceof UnaryOpNode<Attributes> unOp) {
            return lowerUnaryOp(unOp);
        } else if (expr instanceof BinaryOpNode<Attributes> binOp) {
            return lowerBinaryOp(binOp);
        } else if (expr instanceof ReferenceNode<Attributes> reference) {
            return lowerReference(reference);
        } else if (expr instanceof LiteralNode<Attributes> literal) {
            return lowerLiteral(literal);
        }

        return null;
    }

    Expression lowerApply(ApplyNode<Attributes> apply) {
        var type = (Type) apply.meta().meta().sort();
        MutableList<LocalBinding> bindings = Lists.mutable.empty();

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

        var expr = lowerToValue(bindings, appliedExpr);

        var args = appliedArgs.collect(arg -> lowerToValue(bindings, arg));

        var loweredApply = new Apply(type, expr, args);

        return bindings.isEmpty() ? loweredApply : new Block(type, bindings.toImmutableList(), loweredApply);
    }

    Expression lowerBlock(BlockNode<Attributes> block) {
        var type = (Type) block.meta().meta().sort();

        var bindings = block.declarations().collect(let -> {
            var letName = (LocalName) let.meta().meta().name();
            var letType = (Type) let.meta().meta().sort();
            var letExpr = lowerExpr(let.expr());
            return (LocalBinding) new LetAssign(letName, letType, letExpr);
        });

        var result = block.result().map(this::lowerExpr).orElse(Unit.INSTANCE);

        return new Block(type, bindings, result);
    }

    Expression lowerIf(IfNode<Attributes> ifExpr) {
        var type = (Type) ifExpr.meta().meta().sort();
        MutableList<LocalBinding> bindings = Lists.mutable.empty();

        var condition = lowerToValue(bindings, ifExpr.condition());
        var consequent = lowerExpr(ifExpr.consequent());
        var alternative = lowerExpr(ifExpr.alternative());

        var loweredIf = new If(type, condition, consequent, alternative);

        return bindings.isEmpty() ? loweredIf : new Block(type, bindings.toImmutableList(), loweredIf);
    }

    Expression lowerSelect(SelectNode<Attributes> select) {
        // Eliminate selections that aren't applied immediately:
        // To do this, we create a lambda to represent the partial application of the selection
        // receiver.selection ==> (..restArgs) => selection(receiver, ..restArgs)
        var type = (Type) select.meta().meta().sort();
        var funType = (TypeApply) getUnderlyingType(type);

        // The params of the lambda are new synthetic names
        var params = funType.typeArguments()
            .take(funType.typeArguments().size() - 1)
            .collect((paramTy) -> new Param(nameSupply.newSyntheticName(), paramTy));

        // The body of the lambda is an application of the selection to the lowered receiver and any residual args
        MutableList<LocalBinding> bindings = Lists.mutable.empty();
        var receiver = lowerToValue(bindings, select.receiver());
        var residualArgs = params.collect(param -> new Reference(param.name(), param.type()));
        var args = Lists.immutable.of(receiver).newWithAll(residualArgs);
        var selection = lowerReference(select.selection());
        var apply = new Apply(funType.typeArguments().getLast(), selection, args);
        var body = bindings.isEmpty() ? apply : new Block(type, bindings.toImmutableList(), apply);

        return new Lambda(type, params, body);
    }

    Expression lowerLambda(LambdaNode<Attributes> lambda) {
        var type = (Type) lambda.meta().meta().sort();
        var params = lambda.params().collect(this::lowerParam);
        var body = lowerExpr(lambda.body());
        return new Lambda(type, params, body);
    }

    Expression lowerMatch(MatchNode<Attributes> match) {
        var type = (Type) match.meta().meta().sort();
        MutableList<LocalBinding> bindings = Lists.mutable.empty();
        var scrutinee = lowerToValue(bindings, match.scrutinee());
        var cases = match.cases().collect(this::lowerCase);
        return new Match(type, scrutinee, cases);
    }

    Case lowerCase(CaseNode<Attributes> cse) {
        var pattern = lowerPattern(cse.pattern());
        var consequent = lowerExpr(cse.consequent());
        return new Case(pattern, consequent);
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
        }  else if (pattern instanceof ConstructorPatternNode<Attributes> constr) {
            var name = (ConstructorName) constr.meta().meta().name();
            var type = (Type) constr.meta().meta().sort();
            var fields = constr.fields()
                .collect(field -> lowerFieldPattern(name, field));
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

    Expression lowerUnaryOp(UnaryOpNode<Attributes> unOp) {
        var type = (Type) unOp.meta().meta().sort();
        MutableList<LocalBinding> bindings = Lists.mutable.empty();
        var operand = lowerToValue(bindings, unOp.operand());
        var loweredOp = new UnOp(type, unOp.operator(), operand);
        return bindings.isEmpty() ? loweredOp : new Block(type, bindings.toImmutableList(), loweredOp);
    }

    Expression lowerBinaryOp(BinaryOpNode<Attributes> binOp) {
        var type = (Type) binOp.meta().meta().sort();
        MutableList<LocalBinding> bindings = Lists.mutable.empty();
        var left = lowerToValue(bindings, binOp.leftOperand());
        var right = lowerToValue(bindings, binOp.rightOperand());
        var loweredOp = new BinOp(type, left, binOp.operator(), right);
        return bindings.isEmpty() ? loweredOp : new Block(type, bindings.toImmutableList(), loweredOp);
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
