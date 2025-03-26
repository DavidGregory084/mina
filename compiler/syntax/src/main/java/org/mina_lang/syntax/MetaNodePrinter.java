/*
 * SPDX-FileCopyrightText:  © 2024-2025 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.syntax;

import com.opencastsoftware.prettier4j.Doc;
import org.apache.commons.text.StringEscapeUtils;
import org.eclipse.collections.api.list.ImmutableList;
import org.mina_lang.common.Meta;
import org.mina_lang.common.operators.BinaryOp;
import org.mina_lang.common.operators.UnaryOp;

import java.util.Optional;

public class MetaNodePrinter<A> implements MetaNodeFolder<A, Doc> {
    private static final int DEFAULT_INDENT = 3;
    private static final Doc NAMESPACE = Doc.text("namespace");
    private static final Doc IMPORT = Doc.text("import");
    private static final Doc RSLASH = Doc.text("/");
    private static final Doc AS = Doc.text("as");
    private static final Doc LET = Doc.text("let");
    private static final Doc LSQUARE = Doc.text("[");
    private static final Doc RSQUARE = Doc.text("]");
    private static final Doc LPAREN = Doc.text("(");
    private static final Doc RPAREN = Doc.text(")");
    private static final Doc COMMA = Doc.text(",");
    private static final Doc COLON = Doc.text(":");
    private static final Doc EQUAL = Doc.text("=");
    private static final Doc LBRACE = Doc.text("{");
    private static final Doc RBRACE = Doc.text("}");
    private static final Doc SEMI = Doc.text(";");
    private static final Doc MINUS = Doc.text("-");
    private static final Doc NOT = Doc.text("!");
    private static final Doc MUL = Doc.text("*");
    private static final Doc MOD = Doc.text("%");
    private static final Doc PLUS = Doc.text("+");
    private static final Doc SHL = Doc.text("<<");
    private static final Doc SHR = Doc.text(">>");
    private static final Doc USHR = Doc.text(">>>");
    private static final Doc BITWISE_NOT = Doc.text("~");
    private static final Doc BITWISE_XOR = Doc.text("^");
    private static final Doc BITWISE_OR = Doc.text("|");
    private static final Doc BITWISE_AND = Doc.text("&");
    private static final Doc AND = Doc.text("&&");
    private static final Doc OR = Doc.text("||");
    private static final Doc LT = Doc.text("<");
    private static final Doc LTE = Doc.text("<=");
    private static final Doc GT = Doc.text(">");
    private static final Doc GTE = Doc.text(">=");
    private static final Doc EQ = Doc.text("==");
    private static final Doc NEQ = Doc.text("!=");
    private static final Doc IF = Doc.text("if");
    private static final Doc THEN = Doc.text("then");
    private static final Doc ELSE = Doc.text("else");
    private static final Doc ARROW = Doc.text("->");
    private static final Doc MATCH = Doc.text("match");
    private static final Doc WITH = Doc.text("with");
    private static final Doc CASE = Doc.text("case");
    private static final Doc DATA = Doc.text("data");
    private static final Doc DOT = Doc.text(".");
    private static final Doc AT = Doc.text("@");
    private static final Doc SQUOTE = Doc.text("'");
    private static final Doc DQUOTE = Doc.text("\"");
    private static final Doc TRUE = Doc.text("true");
    private static final Doc FALSE = Doc.text("false");
    private static final Doc SPACE = Doc.text(" ");
    private static final Doc LONG_SUFFIX = Doc.text("L");
    private static final Doc FLOAT_SUFFIX = Doc.text("F");

    private final int indent;

    public MetaNodePrinter(int indent) {
        this.indent = indent;
    }

    public MetaNodePrinter() {
        this.indent = DEFAULT_INDENT;
    }

    private Doc indented(Doc expr) {
        return Doc.group(Doc.lineOrSpace().append(expr).indent(this.indent));
    }

    private Doc visitNamespaceId(NamespaceIdNode id) {
        if (!id.pkg().isEmpty()) {
            return Doc.intersperse(RSLASH, id.pkg().stream().map(Doc::text))
                .append(RSLASH).append(Doc.text(id.ns()));
        } else {
            return Doc.text(id.ns());
        }
    }

    private Doc visitTypeParams(ImmutableList<Doc> typeParams) {
        if (typeParams.isEmpty()) {
            return Doc.empty();
        }

        return Doc.intersperse(
            COMMA.append(Doc.lineOrSpace()),
            typeParams.stream()
        ).bracket(this.indent, Doc.lineOrEmpty(), LSQUARE, RSQUARE);
    }

    private Doc visitValueParams(ImmutableList<Doc> valueParams) {
        return Doc.intersperse(
            COMMA.append(Doc.lineOrSpace()),
            valueParams.stream()
        ).bracket(this.indent, Doc.lineOrEmpty(), LPAREN, RPAREN);
    }

    private Doc visitDeclarations(ImmutableList<Doc> declarations) {
        if (declarations.toList().isEmpty()) {
            return LBRACE.append(RBRACE);
        }

        return Doc.intersperse(
            Doc.lineOr(SEMI.append(SPACE)),
            declarations.stream()
        ).bracket(this.indent, Doc.lineOrSpace(), LBRACE, RBRACE);
    }

    private Doc visitSymbols(ImmutableList<Doc> symbols) {
        if (symbols.isEmpty()) {
            return LBRACE.append(RBRACE);
        }

        return Doc.intersperse(
            COMMA.append(Doc.lineOrSpace()),
            symbols.stream()
        ).bracket(this.indent, Doc.lineOrSpace(), LBRACE, RBRACE);
    }

    private Doc visitImport(ImportNode imp) {
        var nsDoc = visitNamespaceId(imp.namespace());
        var impDoc = IMPORT.appendSpace(nsDoc);

        if (imp instanceof ImportQualifiedNode qual) {
            return qual.alias()
                .map(alias -> impDoc.appendSpace(AS).appendSpace(Doc.text(alias)))
                .orElse(impDoc);
        } else if (imp instanceof ImportSymbolsNode sym) {
            var hasMultiple = sym.symbols().size() > 1;
            var hasAliases = sym.symbols().anySatisfy(it -> it.alias().isPresent());
            var symbols = sym.symbols().collect(it -> {
                return it.alias()
                    .map(alias -> Doc.text(it.symbol()).appendSpace(AS).appendSpace(Doc.text(alias)))
                    .orElseGet(() -> Doc.text(it.symbol()));
            });
            return hasMultiple || hasAliases
                ? impDoc.append(DOT).append(visitSymbols(symbols))
                : impDoc.append(DOT).append(symbols.get(0));
        }
        return Doc.empty();
    }

    @Override
    public Doc visitNamespace(Meta<A> meta, NamespaceIdNode id, ImmutableList<ImportNode> imports, ImmutableList<ImmutableList<Doc>> declarationGroups) {
        var bodyDoc = visitDeclarations(
            imports.collect(this::visitImport)
                .newWithAll(declarationGroups.flatCollect(it -> it))
        );

        return NAMESPACE
            .appendSpace(visitNamespaceId(id))
            .appendSpace(bodyDoc);
    }

    @Override
    public Doc visitLet(Meta<A> meta, String name, Optional<Doc> type, Doc expr) {
        var nameDoc = LET.appendSpace(Doc.text(name));
        var typeDoc = type.map(COLON::appendSpace);
        var bodyDoc = EQUAL.append(indented(expr));
        return typeDoc
            .map(typ -> nameDoc.append(typ).appendSpace(bodyDoc))
            .orElseGet(() -> nameDoc.appendSpace(bodyDoc));
    }

    @Override
    public Doc visitLetFn(Meta<A> meta, String name, ImmutableList<Doc> typeParams, ImmutableList<Doc> valueParams, Optional<Doc> returnType, Doc expr) {
        var nameDoc = LET.appendSpace(Doc.text(name));
        var tpArgsDoc = typeParams.isEmpty() ? Doc.empty() : visitTypeParams(typeParams);
        var argsDoc = valueParams.isEmpty() ? Doc.empty() : visitValueParams(valueParams);
        var typeDoc = returnType.map(COLON::appendSpace);
        var bodyDoc = EQUAL.append(indented(expr));
        return typeDoc
            .map(typ -> nameDoc.append(tpArgsDoc).append(argsDoc).append(typ).appendSpace(bodyDoc))
            .orElseGet(() -> nameDoc.append(tpArgsDoc).append(argsDoc).appendSpace(bodyDoc));
    }

    @Override
    public Doc visitParam(Meta<A> param, String name, Optional<Doc> typeAnnotation) {
        return typeAnnotation
            .map(tyAnn -> Doc.text(name).append(COLON).appendSpace(tyAnn))
            .orElseGet(() -> Doc.text(name));
    }

    @Override
    public Doc visitBlock(Meta<A> meta, ImmutableList<Doc> declarations, Optional<Doc> result) {
        return visitDeclarations(declarations.newWithAll(result.stream().toList()));
    }

    @Override
    public Doc visitIf(Meta<A> meta, Doc condition, Doc consequent, Doc alternative) {
        return Doc.group(
            IF.appendSpace(condition)
                .appendLineOrSpace(THEN).appendSpace(consequent)
                .appendLineOrSpace(ELSE).appendSpace(alternative)
        );
    }

    @Override
    public Doc visitLambda(Meta<A> meta, ImmutableList<Doc> params, Doc body) {
        return visitValueParams(params)
            .appendSpace(ARROW)
            .append(indented(body));
    }

    @Override
    public Doc visitMatch(Meta<A> meta, Doc scrutinee, ImmutableList<Doc> cases) {
        return MATCH
            .appendSpace(scrutinee)
            .appendSpace(WITH)
            .appendSpace(visitDeclarations(cases));
    }

    @Override
    public Doc visitApply(Meta<A> meta, Doc expr, ImmutableList<Doc> args) {
        return expr.append(visitValueParams(args));
    }

    @Override
    public Doc visitSelect(Meta<A> meta, Doc receiver, Doc selection) {
        return receiver.bracket(
            this.indent, Doc.lineOrEmpty(),
            LPAREN, RPAREN
        ).append(Doc.group(
            Doc.lineOrEmpty()
                .append(DOT)
                .append(selection)
                .indent(this.indent)
        ));
    }

    @Override
    public Doc visitUnaryOp(Meta<A> meta, UnaryOp operator, Doc operand) {
        var operatorDoc = switch (operator) {
            case NEGATE -> MINUS;
            case BITWISE_NOT -> BITWISE_NOT;
            case BOOLEAN_NOT -> NOT;
        };

        return operatorDoc.append(operand);
    }

    @Override
    public Doc visitBinaryOp(Meta<A> meta, Doc leftOperand, BinaryOp operator, Doc rightOperand) {
        var operatorDoc = switch (operator) {
            case MULTIPLY -> MUL;
            case DIVIDE -> RSLASH;
            case MODULUS -> MOD;
            case ADD -> PLUS;
            case SUBTRACT -> MINUS;
            case SHIFT_LEFT -> SHL;
            case SHIFT_RIGHT -> SHR;
            case UNSIGNED_SHIFT_RIGHT -> USHR;
            case BITWISE_AND -> BITWISE_AND;
            case BITWISE_OR -> BITWISE_OR;
            case BITWISE_XOR -> BITWISE_XOR;
            case LESS_THAN -> LT;
            case LESS_THAN_EQUAL -> LTE;
            case GREATER_THAN -> GT;
            case GREATER_THAN_EQUAL -> GTE;
            case EQUAL -> EQ;
            case NOT_EQUAL -> NEQ;
            case BOOLEAN_AND -> AND;
            case BOOLEAN_OR -> OR;
        };

        return leftOperand
            .appendLineOrSpace(operatorDoc)
            .appendLineOrSpace(rightOperand)
            .bracket(this.indent, Doc.lineOrEmpty(), LPAREN, RPAREN);
    }

    @Override
    public Doc visitReference(Meta<A> meta, QualifiedIdNode id) {
        return Doc.text(id.name());
    }

    @Override
    public Doc visitCase(Meta<A> meta, Doc pattern, Doc consequent) {
        return CASE
            .appendSpace(pattern)
            .appendSpace(ARROW)
            .appendSpace(consequent.indent(this.indent));
    }

    @Override
    public Doc visitData(Meta<A> meta, String name, ImmutableList<Doc> typeParams, ImmutableList<Doc> constructors) {
        return DATA
            .appendSpace(Doc.text(name))
            .append(visitTypeParams(typeParams))
            .appendSpace(visitDeclarations(constructors));
    }

    @Override
    public Doc visitConstructor(Meta<A> meta, String name, ImmutableList<Doc> params, Optional<Doc> type) {
        var constructor = CASE
            .appendSpace(Doc.text(name))
            .append(visitValueParams(params));
        return type
            .map(tp -> constructor.append(COLON.appendSpace(tp)))
            .orElse(constructor);
    }

    @Override
    public Doc visitConstructorParam(Meta<A> meta, String name, Doc typeAnnotation) {
        return visitParam(meta, name, Optional.of(typeAnnotation));
    }

    @Override
    public Doc visitAliasPattern(Meta<A> meta, String alias, Doc pattern) {
        return Doc.text(alias).appendSpace(AT).appendSpace(pattern);
    }

    @Override
    public Doc visitConstructorPattern(Meta<A> meta, QualifiedIdNode id, ImmutableList<Doc> fields) {
        return Doc.text(id.name()).appendSpace(visitSymbols(fields));
    }

    @Override
    public Doc visitFieldPattern(Meta<A> meta, String field, Doc pattern) {
        return Doc.text(field).append(COLON).appendSpace(pattern);
    }

    @Override
    public Doc visitIdPattern(Meta<A> meta, String name) {
        return Doc.text(name);
    }

    @Override
    public Doc visitLiteralPattern(Meta<A> meta, Doc literal) {
        return literal;
    }

    @Override
    public Doc visitBoolean(Meta<A> meta, boolean value) {
        return value ? TRUE : FALSE;
    }

    @Override
    public Doc visitChar(Meta<A> meta, char value) {
        var escapedValue = StringEscapeUtils.escapeJava(String.valueOf(value));
        return SQUOTE.append(Doc.text(escapedValue)).append(SQUOTE);
    }

    @Override
    public Doc visitString(Meta<A> meta, String value) {
        var escapedValue = StringEscapeUtils.escapeJava(value);
        return DQUOTE.append(Doc.text(escapedValue)).append(DQUOTE);
    }

    @Override
    public Doc visitInt(Meta<A> meta, int value) {
        return Doc.text(Integer.toString(value));
    }

    @Override
    public Doc visitLong(Meta<A> meta, long value) {
        return Doc.text(Long.toString(value)).append(LONG_SUFFIX);
    }

    @Override
    public Doc visitFloat(Meta<A> meta, float value) {
        return Doc.text(Float.toString(value)).append(FLOAT_SUFFIX);
    }

    @Override
    public Doc visitDouble(Meta<A> meta, double value) {
        return Doc.text(Double.toString(value));
    }

    @Override
    public Doc visitQuantifiedType(Meta<A> meta, ImmutableList<Doc> args, Doc body) {
        return visitTypeParams(args).appendSpace(body.bracket(this.indent, LBRACE, RBRACE));
    }

    @Override
    public Doc visitFunType(Meta<A> meta, ImmutableList<Doc> argTypes, Doc returnType) {
        return visitValueParams(argTypes)
            .appendSpace(ARROW)
            .appendSpace(returnType);
    }

    @Override
    public Doc visitTypeApply(Meta<A> meta, Doc type, ImmutableList<Doc> args) {
        return type.append(visitTypeParams(args));
    }

    @Override
    public Doc visitTypeReference(Meta<A> meta, QualifiedIdNode id) {
        return Doc.text(id.name());
    }

    @Override
    public Doc visitForAllVar(Meta<A> meta, String name) {
        return Doc.text(name);
    }

    @Override
    public Doc visitExistsVar(Meta<A> meta, String name) {
        return Doc.text(name);
    }
}
