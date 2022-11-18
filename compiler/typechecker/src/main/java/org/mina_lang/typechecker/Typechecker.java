package org.mina_lang.typechecker;

import static org.mina_lang.syntax.SyntaxNodes.*;

import java.util.Optional;
import java.util.function.Supplier;

import org.mina_lang.common.Attributes;
import org.mina_lang.common.Environment;
import org.mina_lang.common.Meta;
import org.mina_lang.common.diagnostics.DiagnosticCollector;
import org.mina_lang.common.names.Name;
import org.mina_lang.common.scopes.Scope;
import org.mina_lang.common.scopes.TypeLambdaScope;
import org.mina_lang.common.types.*;
import org.mina_lang.syntax.*;

import com.opencastsoftware.prettier4j.Doc;

public class Typechecker {

    private DiagnosticCollector diagnostics;
    private Environment<Attributes> environment;
    private TypePrinter typePrinter = new TypePrinter();
    private KindPrinter kindPrinter = new KindPrinter();
    private UnsolvedVariableSupply varSupply = new UnsolvedVariableSupply();

    public Typechecker(DiagnosticCollector diagnostics, Environment<Attributes> globalEnv) {
        this.diagnostics = diagnostics;
        this.environment = globalEnv;
    }

    public Environment<Attributes> getEnvironment() {
        return environment;
    }

    <A> A withScope(Scope<Attributes> scope, Supplier<A> fn) {
        environment.pushScope(scope);
        var result = fn.get();
        environment.popScope(scope.getClass());
        return result;
    }

    void mismatchedKind(Meta<Attributes> meta, Kind expectedType) {
        var expected = expectedType.accept(kindPrinter);
        var actual = ((Kind) meta.meta().sort()).accept(kindPrinter);

        var message = Doc.group(
                Doc.text("Mismatched kind!")
                        .appendLineOrSpace(Doc.text("Expected: ").append(expected))
                        .appendLineOr(Doc.text(", "), Doc.text("Actual: ").append(actual)))
                .render(80);

        diagnostics.reportError(meta.range(), message);
    }

    void mismatchedType(Meta<Attributes> meta, Type expectedType) {
        var expected = expectedType.accept(typePrinter);
        var actual = ((Type) meta.meta().sort()).accept(typePrinter);

        var message = Doc.group(
                Doc.text("Mismatched type!")
                        .appendLineOrSpace(Doc.text("Expected: ").append(expected))
                        .appendLineOr(Doc.text(", "), Doc.text("Actual: ").append(actual)))
                .render(80);

        diagnostics.reportError(meta.range(), message);
    }

    Meta<Attributes> updateMetaWith(Meta<Name> meta, Sort sort) {
        var attributes = meta.meta().withSort(sort);
        return meta.withMeta(attributes);
    }

    public NamespaceNode<Attributes> typecheck(NamespaceNode<Name> namespace) {
        return inferNamespace(namespace);
    }

    public DeclarationNode<Attributes> typecheck(DeclarationNode<Name> node) {
        return inferDeclaration(node);
    }

    public ExprNode<Attributes> typecheck(ExprNode<Name> node) {
        return inferExpr(node);
    }

    public TypeNode<Attributes> typecheck(TypeNode<Name> node) {
        return inferType(node);
    }

    NamespaceNode<Attributes> inferNamespace(NamespaceNode<Name> namespace) {
        var updatedMeta = updateMetaWith(namespace.meta(), Type.NAMESPACE);
        var inferredDecls = namespace.declarations().collect(this::inferDeclaration);
        return namespaceNode(updatedMeta, namespace.id(), namespace.imports(), inferredDecls);
    }

    TypeNode<Attributes> inferType(TypeNode<Name> typ) {
        if (typ instanceof TypeApplyNode<Name> tyApp) {
            var inferredType = inferType(tyApp.type());
            var inferredTypeKind = (Kind) inferredType.meta().meta().sort();

            // Types should be fully applied
            if (inferredTypeKind instanceof HigherKind hk &&
                    hk.argKinds().size() == tyApp.args().size()) {

                var checkedArgs = tyApp.args()
                        .zip(hk.argKinds())
                        .collect(pair -> checkType(pair.getOne(), pair.getTwo()));

                var updatedMeta = updateMetaWith(tyApp.meta(), TypeKind.INSTANCE);

                return typeApplyNode(updatedMeta, inferredType, checkedArgs);

            } else {
                var inferredArgs = tyApp.args()
                        .collect(this::inferType);

                // TODO: Figure out whether hardcoding a proper type here can be wrong vs. user
                // code
                var appliedKind = new HigherKind(
                        inferredArgs.collect(argTy -> (Kind) argTy.meta().meta().sort()),
                        TypeKind.INSTANCE);

                var updatedMeta = updateMetaWith(tyApp.meta(), appliedKind);

                // FIXME: This causes doubling of diagnostics when inferType is called within
                // checkType, which happens a lot
                mismatchedKind(updatedMeta, inferredTypeKind);

                return typeApplyNode(updatedMeta, inferredType, inferredArgs);
            }

        } else if (typ instanceof FunTypeNode<Name> funTy) {
            var inferredArgs = funTy.argTypes()
                    .collect(argTy -> checkType(argTy, TypeKind.INSTANCE));

            var inferredReturn = checkType(funTy.returnType(), TypeKind.INSTANCE);

            // Function types are essentially poly-kinded so any number of args
            // produces a proper type
            var updatedMeta = updateMetaWith(funTy.meta(), TypeKind.INSTANCE);

            return funTypeNode(updatedMeta, inferredArgs, inferredReturn);

        } else if (typ instanceof ForAllVarNode<Name> forall) {
            var unsolvedKind = varSupply.newUnsolvedKind();
            var updatedMeta = updateMetaWith(forall.meta(), unsolvedKind);

            environment.populateType(forall.name(), updatedMeta);

            return forAllVarNode(updatedMeta, forall.name());

        } else if (typ instanceof ExistsVarNode<Name> exists) {
            var unsolvedKind = varSupply.newUnsolvedKind();
            var updatedMeta = updateMetaWith(exists.meta(), unsolvedKind);

            environment.populateType(exists.name(), updatedMeta);

            return existsVarNode(updatedMeta, exists.name());

        } else if (typ instanceof TypeReferenceNode<Name> tyRef) {
            // Lookup from the environment
            var envType = environment.lookupType(tyRef.id().canonicalName()).get();
            var updatedMeta = updateMetaWith(tyRef.meta(), envType.meta().sort());

            return typeRefNode(updatedMeta, tyRef.id());
        }

        return null;
    }

    TypeNode<Attributes> checkType(TypeNode<Name> typ, Kind expectedKind) {
        if (typ instanceof TypeLambdaNode<Name> tyLam) {
            withScope(new TypeLambdaScope<>(), () -> {
                var inferredArgs = tyLam.args()
                    .collect(tyArg -> (TypeVarNode<Attributes>) inferType(tyArg));
                var inferredArgKinds = inferredArgs
                    .collect(tyArg -> (Kind) tyArg.meta().meta().sort());

                // Type lambda should return proper type
                var checkedReturn = checkType(tyLam.body(), TypeKind.INSTANCE);

                var updatedMeta = updateMetaWith(tyLam.meta(), new HigherKind(inferredArgKinds, TypeKind.INSTANCE));

                return typeLambdaNode(updatedMeta, inferredArgs, checkedReturn);
            });
        } else {
            var inferredType = inferType(typ);

            // TODO: Add an error kind to place in Meta when we have a mismatch?
            if (!expectedKind.equals(inferredType.meta().meta().sort())) {
                mismatchedKind(inferredType.meta(), expectedKind);
            }

            return inferredType;
        }

        return null;
    }

    DeclarationNode<Attributes> inferDeclaration(DeclarationNode<Name> declaration) {
        if (declaration instanceof DataNode<Name> data) {

        } else if (declaration instanceof LetFnNode<Name> letFn) {

        } else if (declaration instanceof LetNode<Name> let) {
            return let.type().map(typ -> {
                // Check kind for the type ascription - it should be a proper type
                var kindedType = checkType(typ, TypeKind.INSTANCE);
                // TODO: code to convert from TypeNode to Type in a given Environment
                // var expectedType = kindedType.meta().meta().sort();
                // // Check expr against expected type
                // var checkedExpr = checkExpr(let.expr(), (Type) expectedType);
                return letNode((Meta<Attributes>) null, let.name(), kindedType, null);
            }).orElseGet(() -> {
                // Infer type of expr
                var inferredExpr = inferExpr(let.expr());
                return letNode((Meta<Attributes>) null, null, Optional.empty(), inferredExpr);
            });
        }

        return null;
    }

    ExprNode<Attributes> inferExpr(ExprNode<Name> expr) {
        if (expr instanceof BlockNode<Name> block) {

        } else if (expr instanceof IfNode<Name> ifExpr) {
            var condition = checkExpr(ifExpr.condition(), Type.BOOLEAN);
            var consequent = inferExpr(ifExpr.consequent());
            var consequentType = (Type) consequent.meta().meta().sort();
            var alternative = checkExpr(ifExpr.alternative(), consequentType);
            var attributes = ifExpr.meta().meta().withSort(consequentType);
            var updatedMeta = ifExpr.meta().withMeta(attributes);
            return ifNode(updatedMeta, condition, consequent, alternative);
        } else if (expr instanceof LambdaNode<Name> lambda) {

        } else if (expr instanceof MatchNode<Name> match) {

        } else if (expr instanceof ReferenceNode<Name> reference) {
            // // Lookup from the environment
            // var envType = environment.lookupValue(reference.id().canonicalName()).get();
            // var updatedMeta = updateMetaWith(reference.meta(), envType.meta().sort());
            // return refNode(updatedMeta, reference.id());
        } else if (expr instanceof LiteralNode<Name> literal) {
            return inferLiteral(literal);
        } else if (expr instanceof ApplyNode<Name> apply) {
            // var inferredExpr = inferExpr(apply.expr());
            // var inferredType = (Type) inferredExpr.meta().meta().sort();
            // if (Type.isFunction(inferredType)) {
            // var funType = (TypeApply) inferredType;
            // var argTypes = funType.typeArguments().take(funType.typeArguments().size() -
            // 1);
            // apply.args().collect(this::inferExpr).zip(argTypes);
            // }
        }

        return null;
    }

    LiteralNode<Attributes> inferLiteral(LiteralNode<Name> literal) {
        if (literal instanceof BooleanNode<Name> bool) {
            var updatedMeta = updateMetaWith(bool.meta(), Type.BOOLEAN);
            return boolNode(updatedMeta, bool.value());
        } else if (literal instanceof CharNode<Name> chr) {
            var updatedMeta = updateMetaWith(chr.meta(), Type.CHAR);
            return charNode(updatedMeta, chr.value());
        } else if (literal instanceof StringNode<Name> str) {
            var updatedMeta = updateMetaWith(str.meta(), Type.STRING);
            return stringNode(updatedMeta, str.value());
        } else if (literal instanceof IntNode<Name> intgr) {
            var updatedMeta = updateMetaWith(intgr.meta(), Type.INT);
            return intNode(updatedMeta, intgr.value());
        } else if (literal instanceof LongNode<Name> lng) {
            var updatedMeta = updateMetaWith(lng.meta(), Type.LONG);
            return longNode(updatedMeta, lng.value());
        } else if (literal instanceof DoubleNode<Name> dbl) {
            var updatedMeta = updateMetaWith(dbl.meta(), Type.DOUBLE);
            return doubleNode(updatedMeta, dbl.value());
        } else if (literal instanceof FloatNode<Name> flt) {
            var updatedMeta = updateMetaWith(flt.meta(), Type.FLOAT);
            return floatNode(updatedMeta, flt.value());
        }

        return null;
    }

    ExprNode<Attributes> checkExpr(ExprNode<Name> expr, Type expectedType) {
        if (expr instanceof BlockNode<Name> block) {

        } else if (expr instanceof IfNode<Name> ifExpr) {

        } else if (expr instanceof LambdaNode<Name> lambda) {

        } else if (expr instanceof MatchNode<Name> match) {

        } else if (expr instanceof ReferenceNode<Name> reference) {

        } else if (expr instanceof LiteralNode<Name> literal) {
            return checkLiteral(literal, expectedType);
        } else if (expr instanceof ApplyNode<Name> apply) {

        } else {
            var inferredExpr = inferExpr(expr);

            // TODO: Add an error type to place in Meta when we have a mismatch?
            if (!expectedType.equals(inferredExpr.meta().meta().sort())) {
                mismatchedType(inferredExpr.meta(), expectedType);
            }

            return inferredExpr;
        }

        return null;
    }

    LiteralNode<Attributes> checkLiteral(LiteralNode<Name> literal, Type expectedType) {
        // TODO: Add an error type to place in Meta when we have a mismatch?
        if (literal instanceof BooleanNode<Name> bool) {
            var updatedMeta = updateMetaWith(bool.meta(), Type.BOOLEAN);
            if (!Type.BOOLEAN.equals(expectedType)) {
                mismatchedType(updatedMeta, expectedType);
            }
            return boolNode(updatedMeta, bool.value());
        } else if (literal instanceof CharNode<Name> chr) {
            var updatedMeta = updateMetaWith(chr.meta(), Type.CHAR);
            if (!Type.CHAR.equals(expectedType)) {
                mismatchedType(updatedMeta, expectedType);
            }
            return charNode(updatedMeta, chr.value());
        } else if (literal instanceof StringNode<Name> str) {
            var updatedMeta = updateMetaWith(str.meta(), Type.STRING);
            if (!Type.STRING.equals(expectedType)) {
                mismatchedType(updatedMeta, expectedType);
            }
            return stringNode(updatedMeta, str.value());
        } else if (literal instanceof IntNode<Name> intgr) {
            var updatedMeta = updateMetaWith(intgr.meta(), Type.INT);
            if (!Type.INT.equals(expectedType)) {
                mismatchedType(updatedMeta, expectedType);
            }
            return intNode(updatedMeta, intgr.value());
        } else if (literal instanceof LongNode<Name> lng) {
            var updatedMeta = updateMetaWith(lng.meta(), Type.LONG);
            if (!Type.LONG.equals(expectedType)) {
                mismatchedType(updatedMeta, expectedType);
            }
            return longNode(updatedMeta, lng.value());
        } else if (literal instanceof DoubleNode<Name> dbl) {
            var updatedMeta = updateMetaWith(dbl.meta(), Type.DOUBLE);
            if (!Type.DOUBLE.equals(expectedType)) {
                mismatchedType(updatedMeta, expectedType);
            }
            return doubleNode(updatedMeta, dbl.value());
        } else if (literal instanceof FloatNode<Name> flt) {
            var updatedMeta = updateMetaWith(flt.meta(), Type.FLOAT);
            if (!Type.FLOAT.equals(expectedType)) {
                mismatchedType(updatedMeta, expectedType);
            }
            return floatNode(updatedMeta, flt.value());
        }

        return null;
    }
}
