grammar Mina;

@header {
	import java.text.Normalizer;
	import java.text.Normalizer.Form;
}

// Files
compilationUnit: module* EOF;

// Modules
module: MODULE moduleId LBRACE importDeclaration* declaration* RBRACE;

// Imports
importDeclaration : IMPORT importSelector;

importSelector : moduleId (DOT ID)? | moduleId DOT LBRACE importSymbols RBRACE;

importSymbols : (ID COMMA)* ID ;

// Declarations
declaration: dataDeclaration | letDeclaration;

dataDeclaration: DATA ID LBRACE RBRACE;

letDeclaration: LET ID EQ expr;

// Expressions
expr: ifExpr | lambdaExpr | matchExpr | literal | applicableExpr;

ifExpr: IF expr THEN expr ELSE expr;

lambdaExpr: lambdaParams ARROW expr;

lambdaParams: ID | LPAREN RPAREN | LPAREN (ID COMMA)* ID RPAREN;

matchExpr : MATCH expr WITH LBRACE matchCase* RBRACE;

matchCase : CASE pattern ARROW expr ;

pattern : ID | literal | constructorPattern ;

constructorPattern: patternAlias? qualifiedId LBRACE fieldPatterns? RBRACE ;

fieldPatterns : (fieldPattern COMMA)* fieldPattern ;

fieldPattern : ID (COLON pattern)? ;

patternAlias : ID AT ;

applicableExpr: parenExpr | qualifiedId | applicableExpr application;

application : LPAREN RPAREN | LPAREN (expr COMMA)* expr RPAREN;

parenExpr: LPAREN expr RPAREN;

// Literals
literal: literalBoolean | literalInt | literalChar;

literalBoolean: TRUE | FALSE;
literalInt: LITERAL_INT;
literalChar: LITERAL_CHAR;

// Identifiers
moduleId: (ID RSLASH)* ID;
qualifiedId: (moduleId DOT)? ID;

WHITESPACE: WS+ -> skip;

// Package separator
RSLASH: '/';

// Module header
MODULE: 'module';
IMPORT: 'import';

// Top level declarations
DATA: 'data';
LET: 'let';

// Control statements
IF: 'if';
THEN: 'then';
ELSE: 'else';

MATCH: 'match';
WITH: 'with';
CASE: 'case';

// Block and application delimiters
LBRACE: '{';
RBRACE: '}';
LPAREN: '(';
RPAREN: ')';
LSQUARE : '[';
RSQUARE : ']';

// Reserved operators
EQ: '=';
DOT: '.';
COMMA: ',';
ARROW: '->';
AT: '@';
SEMICOLON: ';';
COLON: ':';

// Boolean literals
TRUE: 'true';
FALSE: 'false';

// String and char literals
SQUOTE: '\'';
DQUOTE: '"';

LITERAL_CHAR: SQUOTE SINGLE_CHAR SQUOTE;

// Numeric literals
LITERAL_INT: DECIMAL_NUMERAL INTEGER_SUFFIX?;

// Identifiers (normalized)
ID: ID_START ID_CONTINUE* { setText(Normalizer.normalize(getText(), Form.NFKC)); };

fragment WS: [ \t\r\n];

fragment ID_START: [_\p{XID_Start}];
fragment ID_CONTINUE: [\p{XID_Continue}];

fragment SINGLE_CHAR: ~['\r\n\\];

fragment DECIMAL_NUMERAL:
	'0'
	| NON_ZERO_DIGIT DIGITS?
	| NON_ZERO_DIGIT UNDERSCORES DIGITS;

fragment DIGITS: DIGIT | DIGIT DIGITS_AND_UNDERSCORES? DIGIT;
fragment DIGITS_AND_UNDERSCORES: DIGIT_OR_UNDERSCORE+;
fragment DIGIT_OR_UNDERSCORE: [_0-9];
fragment UNDERSCORES: '_'+;
fragment DIGIT: [0-9];
fragment NON_ZERO_DIGIT: [1-9];
fragment INTEGER_SUFFIX: [iIlL];
