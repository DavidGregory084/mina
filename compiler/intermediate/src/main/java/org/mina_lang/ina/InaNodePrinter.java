/*
 * SPDX-FileCopyrightText:  © 2025 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.ina;

import com.opencastsoftware.prettier4j.Doc;
import org.apache.commons.text.StringEscapeUtils;
import org.mina_lang.common.names.*;
import org.mina_lang.common.operators.BinaryOp;
import org.mina_lang.common.operators.UnaryOp;
import org.mina_lang.common.types.Type;
import org.mina_lang.common.types.TypePrinter;
import org.mina_lang.common.types.TypeVar;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class InaNodePrinter implements InaNodeFolder<Doc> {
    private static final int DEFAULT_INDENT = 3;
    private static final Doc NAMESPACE = Doc.text("namespace");
    private static final Doc RSLASH = Doc.text("/");
    private static final Doc LET = Doc.text("let");
    private static final Doc JOIN = Doc.text("join");
    private static final Doc LSQUARE = Doc.text("[");
    private static final Doc RSQUARE = Doc.text("]");
    private static final Doc LPAREN = Doc.text("(");
    private static final Doc RPAREN = Doc.text(")");
    private static final Doc COMMA = Doc.text(",").append(Doc.lineOrSpace());
    private static final Doc COLON = Doc.text(":");
    private static final Doc EQUAL = Doc.text("=");
    private static final Doc LBRACE = Doc.text("{");
    private static final Doc RBRACE = Doc.text("}");
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
    private static final Doc AT = Doc.text("@");
    private static final Doc SQUOTE = Doc.text("'");
    private static final Doc DQUOTE = Doc.text("\"");
    private static final Doc TRUE = Doc.text("true");
    private static final Doc FALSE = Doc.text("false");
    private static final Doc LONG_SUFFIX = Doc.text("L");
    private static final Doc FLOAT_SUFFIX = Doc.text("F");
    private static final Doc BOX = Doc.text("box");
    private static final Doc UNBOX = Doc.text("unbox");
    private static final Doc EMPTY_DECLS = LBRACE.append(RBRACE);
    private static final Doc EMPTY_LINE = Doc.line().appendLine(Doc.line());

    private final int indent;

    private final NamePrinter namePrinter;
    private final TypePrinter typePrinter;

    public InaNodePrinter(int indent) {
        this.indent = indent;
        this.namePrinter = new NamePrinter();
        this.typePrinter = new TypePrinter(indent);
    }

    public InaNodePrinter() {
        this(DEFAULT_INDENT);
    }

    private Doc visitTypeParams(List<Doc> typeParams) {
        if (typeParams.isEmpty()) {
            return Doc.empty();
        }

        return Doc
            .intersperse(COMMA, typeParams.stream())
            .bracket(this.indent, Doc.lineOrEmpty(), LSQUARE, RSQUARE);
    }

    private Doc visitValueParams(List<Doc> valueParams) {
        return Doc
            .intersperse(COMMA, valueParams.stream())
            .bracket(this.indent, Doc.lineOrEmpty(), LPAREN, RPAREN);
    }

    private Doc visitSymbols(List<Doc> symbols) {
        if (symbols.isEmpty()) {
            return EMPTY_DECLS;
        }

        return Doc
            .intersperse(COMMA, symbols.stream())
            .bracket(this.indent, Doc.lineOrSpace(), LBRACE, RBRACE);
    }

    private Doc visitDeclarations(List<Doc> declarations) {
        if (declarations.isEmpty()) {
            return EMPTY_DECLS;
        }

        return Doc
            .intersperse(Doc.line(), declarations.stream())
            .bracket(this.indent, Doc.line(), LBRACE, RBRACE);
    }

    private Doc visitTopLevelDeclarations(List<Doc> declarations) {
        if (declarations.isEmpty()) {
            return EMPTY_DECLS;
        }

        return Doc
            .intersperse(EMPTY_LINE, declarations.stream())
            .bracket(this.indent, Doc.line(), LBRACE, RBRACE);
    }

    @Override
    public Doc visitNamespace(NamespaceName name, List<Doc> declarations) {
        return NAMESPACE
            .appendSpace(name.accept(namePrinter))
            .appendSpace(visitTopLevelDeclarations(declarations));
    }

    @Override
    public Doc visitData(DataName name, List<TypeVar> typeParams, List<Doc> constructors) {
        return DATA
            .appendSpace(name.accept(namePrinter))
            .append(visitTypeParams(typeParams.stream().map(tyParam -> tyParam.accept(typePrinter)).toList()))
            .appendSpace(visitDeclarations(constructors));
    }

    @Override
    public Doc visitConstructor(ConstructorName name, List<Doc> fields) {
        return CASE
            .appendSpace(name.accept(namePrinter))
            .append(visitValueParams(fields));
    }

    @Override
    public Doc visitField(FieldName name, Type type) {
        return name
            .accept(namePrinter)
            .append(COLON)
            .appendSpace(type.accept(typePrinter));
    }

    @Override
    public Doc visitLet(LetName name, Type type, Doc body) {
        return LET
            .appendSpace(name.accept(namePrinter))
            .append(COLON)
            .appendSpace(type.accept(typePrinter))
            .appendSpace(EQUAL)
            .appendLineOrSpace(body);
    }

    @Override
    public Doc visitParam(LocalBindingName name, Type type) {
        return name
            .accept(namePrinter)
            .append(COLON)
            .appendSpace(type.accept(typePrinter));
    }

    @Override
    public Doc visitLetAssign(LocalBindingName name, Type type, Doc body) {
        return LET
            .appendSpace(name.accept(namePrinter))
            .append(COLON)
            .appendSpace(type.accept(typePrinter))
            .appendSpace(EQUAL)
            .appendLineOrSpace(body);
    }

    @Override
    public Doc visitJoin(LocalBindingName name, Type type, List<Doc> params, Doc body) {
        return JOIN
            .appendSpace(name.accept(namePrinter))
            .append(COLON)
            .appendSpace(type.accept(typePrinter))
            .appendSpace(EQUAL)
            .appendSpace(visitValueParams(params))
            .appendSpace(ARROW)
            .appendLineOrSpace(body);
    }

    @Override
    public Doc visitApply(Type type, Doc expr, List<Doc> args) {
        return expr.append(visitValueParams(args));
    }

    @Override
    public Doc visitBinOp(Type type, Doc left, BinaryOp operator, Doc right) {
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

        return left
            .appendLineOrSpace(operatorDoc)
            .appendLineOrSpace(right);
    }

    @Override
    public Doc visitBlock(Type type, List<Doc> bindings, Doc result) {
        var declarations = new ArrayList<>(bindings);
        declarations.add(result);
        return visitDeclarations(declarations);
    }

    @Override
    public Doc visitIf(Type type, Doc cond, Doc consequent, Doc alternative) {
        return Doc.group(
            IF.appendSpace(cond)
                .appendLineOrSpace(THEN).appendSpace(consequent)
                .appendLineOrSpace(ELSE).appendSpace(alternative)
        );
    }

    @Override
    public Doc visitLambda(Type type, List<Doc> params, Doc body) {
        return visitValueParams(params)
            .appendSpace(ARROW)
            .appendLineOrSpace(body);
    }

    @Override
    public Doc visitReference(ValueName name, Type type) {
        return name.accept(namePrinter);
    }

    @Override
    public Doc visitUnOp(Type type, UnaryOp operator, Doc operand) {
        var operatorDoc = switch (operator) {
            case NEGATE -> MINUS;
            case BITWISE_NOT -> BITWISE_NOT;
            case BOOLEAN_NOT -> NOT;
        };

        return operatorDoc.append(operand);
    }

    @Override
    public Doc visitMatch(Type type, Doc scrutinee, List<Doc> cases) {
        return MATCH
            .appendSpace(scrutinee)
            .appendSpace(WITH)
            .appendSpace(visitDeclarations(cases));
    }

    @Override
    public Doc visitCase(Doc pattern, Doc consequent) {
        return CASE
            .appendSpace(pattern)
            .appendSpace(ARROW)
            .appendLineOrSpace(consequent);
    }

    @Override
    public Doc visitAliasPattern(LocalName alias, Type type, Doc pattern) {
        return alias.accept(namePrinter).appendSpace(AT).appendSpace(pattern);
    }

    @Override
    public Doc visitConstructorPattern(ConstructorName name, Type type, List<Doc> fields) {
        return name.accept(namePrinter).appendSpace(visitSymbols(fields));
    }

    @Override
    public Doc visitFieldPattern(FieldName name, Type type, Doc pattern) {
        return name.accept(namePrinter).append(COLON).appendSpace(pattern);
    }

    @Override
    public Doc visitIdPattern(LocalName name, Type type) {
        return name.accept(namePrinter);
    }

    @Override
    public Doc visitLiteralPattern(Doc literal) {
        return literal;
    }

    @Override
    public Doc visitBoolean(boolean value) {
        return value ? TRUE : FALSE;
    }

    @Override
    public Doc visitChar(char value) {
        var escapedValue = StringEscapeUtils.escapeJava(java.lang.String.valueOf(value));
        return SQUOTE.append(Doc.text(escapedValue)).append(SQUOTE);
    }

    @Override
    public Doc visitInt(int value) {
        return Doc.text(Integer.toString(value));
    }

    @Override
    public Doc visitLong(long value) {
        return Doc.text(java.lang.Long.toString(value)).append(LONG_SUFFIX);
    }

    @Override
    public Doc visitFloat(float value) {
        return Doc.text(java.lang.Float.toString(value)).append(FLOAT_SUFFIX);
    }

    @Override
    public Doc visitDouble(double value) {
        return Doc.text(java.lang.Double.toString(value));
    }

    @Override
    public Doc visitString(java.lang.String value) {
        var escapedValue = StringEscapeUtils.escapeJava(value);
        return DQUOTE.append(Doc.text(escapedValue)).append(DQUOTE);
    }

    @Override
    public Doc visitUnit() {
        return LBRACE.append(RBRACE);
    }

    @Override
    public Doc visitBox(Doc value) {
        return BOX.append(value.bracket(this.indent, Doc.lineOrEmpty(), LPAREN, RPAREN));
    }

    @Override
    public Doc visitUnbox(Doc value) {
        return UNBOX.append(value.bracket(this.indent, Doc.lineOrEmpty(), LPAREN, RPAREN));
    }
}
