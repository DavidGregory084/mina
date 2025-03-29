/*
 * SPDX-FileCopyrightText:  © 2022-2025 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
parser grammar MinaParser;

options {
    superClass=org.antlr.v4.runtime.Parser;
    tokenVocab=MinaLexer;
}

@parser::members {
public MinaParser(TokenStream input, ThreadLocal<DFA[]> decisionToDFA, ThreadLocal<PredictionContextCache> contextCache) {
    super(input);
    _interp = new ParserATNSimulator(this, _ATN, decisionToDFA.get(), contextCache.get());
}
}

// Namespaces
namespace: NAMESPACE namespaceId LBRACE importDeclaration* declaration* RBRACE EOF;

// Imports
importDeclaration: importQualified | importSymbols;

importQualified:
    IMPORT namespaceId (AS alias = ID)?;

importSymbols:
    IMPORT namespaceId DOT id = ID
    | IMPORT namespaceId DOT LBRACE importee (COMMA importee)* RBRACE;

importee:
    id = ID (AS alias = ID)?;

// Declarations
declaration: dataDeclaration | letFnDeclaration | letDeclaration;

dataDeclaration:
    DATA ID typeParams? LBRACE dataConstructor* RBRACE;

letFnDeclaration: LET ID typeParams? lambdaParams typeAnnotation? EQUAL expr;

letDeclaration: LET ID typeAnnotation? EQUAL expr;

// Data constructors
dataConstructor: CASE ID constructorParams typeAnnotation?;

constructorParams:
    LPAREN RPAREN
    | LPAREN constructorParam (COMMA constructorParam)* RPAREN;

constructorParam: ID typeAnnotation;

// Types
typeAnnotation: COLON type;

type: quantifiedType | funType | applicableType;

quantifiedType: typeParams LBRACE type RBRACE;

typeParams: LSQUARE typeVar (COMMA typeVar)* RSQUARE;

funType: (quantifiedType | applicableType | funTypeParams) ARROW type;

funTypeParams: LPAREN RPAREN | LPAREN type (COMMA type)* RPAREN;

applicableType:
    parenType
    | typeReference
    | applicableType typeApplication;

typeApplication:
    LSQUARE type (COMMA type)* RSQUARE;

parenType: LSQUARE type RSQUARE;

typeReference: qualifiedId | existsVar;

typeVar: forAllVar | existsVar;

forAllVar: ID;

existsVar: QUESTION ID?;

// Expressions
expr:
    // If and lambda cannot be used with operators without parentheses because they are not guaranteed to have a closing delimiter for their trailing expressions.
    // It would be ambiguous whether a trailing application belonged to the final expression inside a lambda's body or to the entire lambda.
    // The same problem exists for the `else` expression in an if.
    // We place `match` at the same level for uniformity with other control flow expressions.
    ifExpr
    | lambdaExpr
    | matchExpr
    | orExpr;

blockExpr: LBRACE letDeclaration* expr? RBRACE;

ifExpr: IF expr THEN expr ELSE expr;

lambdaExpr: (ID | lambdaParams) ARROW expr;

lambdaParams:
    LPAREN RPAREN
    | LPAREN lambdaParam (COMMA lambdaParam)* RPAREN;

lambdaParam: ID typeAnnotation?;

matchExpr: MATCH expr WITH LBRACE matchCase* RBRACE;

matchCase: CASE pattern ARROW expr;

pattern: aliasPattern | idPattern | literalPattern | constructorPattern;

aliasPattern: ID AT pattern;

idPattern: ID;

literalPattern: literal;

constructorPattern: qualifiedId LBRACE fieldPatterns? RBRACE;

fieldPatterns: fieldPattern (COMMA fieldPattern)*;

fieldPattern: ID (COLON pattern)?;

// Operator precedence hierarchy, left factored
orExpr:
    leftOperand = andExpr (DOUBLE_PIPE rightOperand += andExpr)*;

andExpr:
    leftOperand = eqExpr (DOUBLE_AMPERSAND rightOperand += eqExpr)*;

eqExpr:
    leftOperand = relExpr (operator += (DOUBLE_EQUAL | NOT_EQUAL) rightOperand += relExpr)*;

relExpr:
    leftOperand = bitOrExpr (operator += (LESS_THAN | LESS_THAN_EQUAL | GREATER_THAN | GREATER_THAN_EQUAL) rightOperand += bitOrExpr)*;

bitOrExpr:
    leftOperand = bitAndExpr (operator += (CARET | PIPE) rightOperand += bitAndExpr)*;

bitAndExpr:
    leftOperand = shiftExpr (AMPERSAND rightOperand += shiftExpr)*;

shiftExpr:
    leftOperand = addExpr (operator += (LEFT_SHIFT | RIGHT_SHIFT | UNSIGNED_RIGHT_SHIFT) rightOperand += addExpr)*;

addExpr:
    leftOperand = multExpr (operator += (PLUS | MINUS) rightOperand += multExpr)*;

multExpr:
    leftOperand = unaryExpr (operator += (ASTERISK | RSLASH | PERCENT) rightOperand += unaryExpr)*;

unaryExpr:
    postfixExpr
    | operator = (MINUS | EXCLAMATION | TILDE) unaryOperand = unaryExpr;

postfixExpr:
    primaryExpr (postfixOp)*;

postfixOp:
    DOT selection = ID
    | application;

primaryExpr:
    id = ID
    | literal
    | parenExpr
    | blockExpr;

parenExpr: LPAREN expr RPAREN;

application: LPAREN RPAREN | LPAREN expr (COMMA expr)* RPAREN;

// Literals
literal:
    literalBoolean
    | literalInt
    | literalFloat
    | literalChar
    | literalString;

literalBoolean: TRUE | FALSE;
literalInt: LITERAL_INT;
literalFloat: LITERAL_FLOAT;
literalChar: LITERAL_CHAR;
literalString: LITERAL_STRING;

// Identifiers
namespaceId: (pkg += ID RSLASH)* ns = ID;
qualifiedId: (ns = ID DOT)? id = ID;
