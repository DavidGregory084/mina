package org.mina_lang.typechecker;

import static org.mina_lang.syntax.SyntaxNodes.*;

import java.util.Optional;
import java.util.function.Supplier;

import org.mina_lang.common.Attributes;
import org.mina_lang.common.Environment;
import org.mina_lang.common.Meta;
import org.mina_lang.common.diagnostics.DiagnosticCollector;
import org.mina_lang.common.names.Name;
import org.mina_lang.common.scopes.NamespaceScope;
import org.mina_lang.common.scopes.Scope;
import org.mina_lang.common.types.Sort;
import org.mina_lang.common.types.Type;
import org.mina_lang.common.types.TypePrinter;
import org.mina_lang.common.types.UnsolvedVariableSupply;
import org.mina_lang.syntax.*;

import com.opencastsoftware.prettier4j.Doc;

public class Typechecker {

    private DiagnosticCollector diagnostics;
    private Environment<Attributes> environment;
    private UnsolvedVariableSupply varSupply;
    private Kindchecker kindchecker;
    private TypePrinter typePrinter = new TypePrinter();

    public Typechecker(DiagnosticCollector diagnostics, Environment<Attributes> environment) {
        this.diagnostics = diagnostics;
        this.environment = environment;
        this.varSupply = new UnsolvedVariableSupply();
        this.kindchecker = new Kindchecker(diagnostics, environment, varSupply);
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

    Meta<Attributes> updateMetaWith(Meta<Name> meta, Sort sort) {
        var attributes = meta.meta().withSort(sort);
        return meta.withMeta(attributes);
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

    public NamespaceNode<Attributes> typecheck(NamespaceNode<Name> namespace) {
        return inferNamespace(namespace);
    }

    public DeclarationNode<Attributes> typecheck(DeclarationNode<Name> node) {
        return inferDeclaration(node);
    }

    public ExprNode<Attributes> typecheck(ExprNode<Name> node) {
        return inferExpr(node);
    }

    NamespaceNode<Attributes> inferNamespace(NamespaceNode<Name> namespace) {
        return withScope(new NamespaceScope<>(namespace.getName()), () -> {
            var updatedMeta = updateMetaWith(namespace.meta(), Type.NAMESPACE);
            var inferredDecls = namespace.declarations().collect(this::inferDeclaration);
            return namespaceNode(updatedMeta, namespace.id(), namespace.imports(), inferredDecls);
        });
    }

    DeclarationNode<Attributes> inferDeclaration(DeclarationNode<Name> declaration) {
        if (declaration instanceof DataNode<Name> data) {

        } else if (declaration instanceof LetFnNode<Name> letFn) {

        } else if (declaration instanceof LetNode<Name> let) {
            return let.type().map(typ -> {
                var kindedType = kindchecker.kindcheck(typ);
                var expectedType = kindedType.accept(new TypeAnnotationFolder(environment));
                var checkedExpr = checkExpr(let.expr(), expectedType);
                var updatedMeta = updateMetaWith(let.meta(), (Type) checkedExpr.meta().meta().sort());
                return letNode(updatedMeta, let.name(), kindedType, checkedExpr);
            }).orElseGet(() -> {
                var inferredExpr = inferExpr(let.expr());
                var updatedMeta = updateMetaWith(let.meta(), (Type) inferredExpr.meta().meta().sort());
                return letNode(updatedMeta, let.name(), Optional.empty(), inferredExpr);
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
