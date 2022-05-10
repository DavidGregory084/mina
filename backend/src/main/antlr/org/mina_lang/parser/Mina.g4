grammar Mina;

compilationUnit: module* EOF;

module: MODULE moduleId LBRACE importDeclaration* declaration* RBRACE;

importDeclaration : IMPORT importSelector;

importSelector : qualifiedId | moduleId | moduleId DOT LBRACE importSymbols RBRACE;

importSymbols : (ID COMMA)* ID ;

declaration: dataDeclaration | letDeclaration;

dataDeclaration: DATA ID LBRACE RBRACE;

letDeclaration: LET ID EQ expr;

expr: ifExpr | lambdaExpr | literal | applicableExpr;

ifExpr: IF expr THEN expr ELSE expr;

lambdaExpr: lambdaParams ARROW expr;
lambdaParams: LPAREN RPAREN | LPAREN (ID COMMA)* ID RPAREN;

applicableExpr: parenExpr | qualifiedId | applicableExpr application;

application : LPAREN arguments RPAREN;

arguments : (expr COMMA)* expr ;

parenExpr: LPAREN expr RPAREN;

literal: literalBoolean | literalInt | literalChar;

literalBoolean: TRUE | FALSE;
literalInt: LITERAL_INT;
literalChar: LITERAL_CHAR;

packageId: (ID RSLASH)* ID;
moduleId: (packageId RSLASH)? ID;
qualifiedId: (moduleId DOT)? ID;

WHITESPACE: WS+ -> skip;

MODULE: 'module';
IMPORT: 'import';
DATA: 'data';
LET: 'let';
IF: 'if';
THEN: 'then';
ELSE: 'else';

RSLASH: '/';

LBRACE: '{';
RBRACE: '}';
LPAREN: '(';
RPAREN: ')';

EQ: '=';
DOT: '.';
COMMA: ',';
ARROW: '->';

TRUE: 'true';
FALSE: 'false';

ID: ID_START ID_CONTINUE*;

SQUOTE: '\'';
DQUOTE: '"';

LITERAL_INT: DECIMAL_NUMERAL INTEGER_SUFFIX?;
LITERAL_CHAR: SQUOTE SINGLE_CHAR SQUOTE;

fragment WS: [\p{Pattern_White_Space}];

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
