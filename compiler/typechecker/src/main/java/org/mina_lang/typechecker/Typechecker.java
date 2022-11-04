package org.mina_lang.typechecker;

import org.mina_lang.common.Attributes;
import org.mina_lang.common.Environment;
import org.mina_lang.common.Meta;
import org.mina_lang.common.diagnostics.DiagnosticCollector;
import org.mina_lang.common.names.Name;
import org.mina_lang.common.types.Type;
import org.mina_lang.common.types.TypePrinter;
import org.mina_lang.syntax.*;

import com.opencastsoftware.prettier4j.Doc;

import static org.mina_lang.syntax.SyntaxNodes.*;

public class Typechecker {

    private DiagnosticCollector diagnostics;
    private Environment<Attributes> environment;
    private TypePrinter typePrinter = new TypePrinter();

    public Typechecker(DiagnosticCollector diagnostics, Environment<Attributes> globalEnv) {
        this.diagnostics = diagnostics;
        this.environment = globalEnv;
    }

    public Environment<Attributes> getEnvironment() {
        return environment;
    }

    public void mismatchedType(Meta<Attributes> meta, Type expectedType) {
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
        return null;
    }

    DeclarationNode<Attributes> inferDeclaration(DeclarationNode<Name> declaration) {
        if (declaration instanceof DataNode<Name> data) {

        } else if (declaration instanceof LetFnNode<Name> letFn) {

        } else if (declaration instanceof LetNode<Name> let) {

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

        } else if (expr instanceof LiteralNode<Name> literal) {
            return inferLiteral(literal);
        } else if (expr instanceof ApplyNode<Name> apply) {

        }

        return null;
    }

    LiteralNode<Attributes> inferLiteral(LiteralNode<Name> literal) {
        if (literal instanceof BooleanNode<Name> bool) {
            var attributes = bool.meta().meta().withSort(Type.BOOLEAN);
            var updatedMeta = bool.meta().withMeta(attributes);
            return boolNode(updatedMeta, bool.value());
        } else if (literal instanceof CharNode<Name> chr) {
            var attributes = chr.meta().meta().withSort(Type.CHAR);
            var updatedMeta = chr.meta().withMeta(attributes);
            return charNode(updatedMeta, chr.value());
        } else if (literal instanceof StringNode<Name> str) {
            var attributes = str.meta().meta().withSort(Type.STRING);
            var updatedMeta = str.meta().withMeta(attributes);
            return stringNode(updatedMeta, str.value());
        } else if (literal instanceof IntNode<Name> intgr) {
            var attributes = intgr.meta().meta().withSort(Type.INT);
            var updatedMeta = intgr.meta().withMeta(attributes);
            return intNode(updatedMeta, intgr.value());
        } else if (literal instanceof LongNode<Name> lng) {
            var attributes = lng.meta().meta().withSort(Type.LONG);
            var updatedMeta = lng.meta().withMeta(attributes);
            return longNode(updatedMeta, lng.value());
        } else if (literal instanceof DoubleNode<Name> dbl) {
            var attributes = dbl.meta().meta().withSort(Type.DOUBLE);
            var updatedMeta = dbl.meta().withMeta(attributes);
            return doubleNode(updatedMeta, dbl.value());
        } else if (literal instanceof FloatNode<Name> flt) {
            var attributes = flt.meta().meta().withSort(Type.FLOAT);
            var updatedMeta = flt.meta().withMeta(attributes);
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

        }

        return null;
    }

    LiteralNode<Attributes> checkLiteral(LiteralNode<Name> literal, Type expectedType) {
        // TODO: Add an error type to place in Meta when we have a mismatch?
        if (literal instanceof BooleanNode<Name> bool) {
            var attributes = bool.meta().meta().withSort(Type.BOOLEAN);
            var updatedMeta = bool.meta().withMeta(attributes);
            if (expectedType != Type.BOOLEAN) {
                mismatchedType(updatedMeta, expectedType);
            }
            return boolNode(updatedMeta, bool.value());
        } else if (literal instanceof CharNode<Name> chr) {
            var attributes = chr.meta().meta().withSort(Type.CHAR);
            var updatedMeta = chr.meta().withMeta(attributes);
            if (expectedType != Type.CHAR) {
                mismatchedType(updatedMeta, expectedType);
            }
            return charNode(updatedMeta, chr.value());
        } else if (literal instanceof StringNode<Name> str) {
            var attributes = str.meta().meta().withSort(Type.STRING);
            var updatedMeta = str.meta().withMeta(attributes);
            if (expectedType != Type.STRING) {
                mismatchedType(updatedMeta, expectedType);
            }
            return stringNode(updatedMeta, str.value());
        } else if (literal instanceof IntNode<Name> intgr) {
            var attributes = intgr.meta().meta().withSort(Type.INT);
            var updatedMeta = intgr.meta().withMeta(attributes);
            if (expectedType != Type.INT) {
                mismatchedType(updatedMeta, expectedType);
            }
            return intNode(updatedMeta, intgr.value());
        } else if (literal instanceof LongNode<Name> lng) {
            var attributes = lng.meta().meta().withSort(Type.LONG);
            var updatedMeta = lng.meta().withMeta(attributes);
            if (expectedType != Type.LONG) {
                mismatchedType(updatedMeta, expectedType);
            }
            return longNode(updatedMeta, lng.value());
        } else if (literal instanceof DoubleNode<Name> dbl) {
            var attributes = dbl.meta().meta().withSort(Type.DOUBLE);
            var updatedMeta = dbl.meta().withMeta(attributes);
            if (expectedType != Type.DOUBLE) {
                mismatchedType(updatedMeta, expectedType);
            }
            return doubleNode(updatedMeta, dbl.value());
        } else if (literal instanceof FloatNode<Name> flt) {
            var attributes = flt.meta().meta().withSort(Type.FLOAT);
            var updatedMeta = flt.meta().withMeta(attributes);
            if (expectedType != Type.FLOAT) {
                mismatchedType(updatedMeta, expectedType);
            }
            return floatNode(updatedMeta, flt.value());
        }

        return null;
    }
}
