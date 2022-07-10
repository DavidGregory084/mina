grammar Mina;

@header {
import java.text.Normalizer;
import java.text.Normalizer.Form;
}

// Files
compilationUnit: module* EOF;

// Modules
module:
	MODULE moduleId LBRACE importDeclaration* declaration* RBRACE;

// Imports
importDeclaration: IMPORT importSelector;

importSelector:
	moduleId (DOT ID)?
	| moduleId DOT LBRACE importSymbols RBRACE;

importSymbols: (ID COMMA)* ID;

// Declarations
declaration: dataDeclaration | letDeclaration;

dataDeclaration:
	DATA ID typeParams? LBRACE dataConstructor* RBRACE;

letDeclaration: LET ID typeAnnotation? EQ expr;

// Data constructors
dataConstructor: CASE ID (LPAREN constructorParams RPAREN)?;

constructorParams: (constructorParam COMMA)* constructorParam;

constructorParam: ID typeAnnotation;

// Types
type: typeBinder | funType | applicableType;

typeBinder: typeParams LBRACE type RBRACE;

funType: funTypeParams ARROW type;

funTypeParams:
	applicableType
	| LPAREN RPAREN
	| LPAREN (type COMMA)* type RPAREN;

applicableType:
	| parenType
	| typeReference
	| applicableType typeApplication;

typeApplication:
	LSQUARE (typeReference COMMA)* typeReference RSQUARE;

typeParams: LSQUARE (typeVar COMMA)* typeVar RSQUARE;

typeAnnotation: COLON type;

parenType: LPAREN type RPAREN;

// TODO: add qualifiedType instead?
typeReference: qualifiedId | typeVar;

typeVar: QUESTION? ID;

// Expressions
expr:
	ifExpr
	| lambdaExpr
	| matchExpr
	| literal
	| applicableExpr;

ifExpr: IF expr THEN expr ELSE expr;

lambdaExpr: lambdaParams ARROW expr;

lambdaParams:
	lambdaParam
	| LPAREN RPAREN
	| LPAREN (lambdaParam COMMA)* lambdaParam RPAREN;

lambdaParam: ID typeAnnotation?;

matchExpr: MATCH expr WITH LBRACE matchCase* RBRACE;

matchCase: CASE pattern ARROW expr;

pattern: idPattern | literalPattern | constructorPattern;

idPattern: patternAlias? ID;

literalPattern: patternAlias? literal;

constructorPattern:
	patternAlias? qualifiedId LBRACE fieldPatterns? RBRACE;

fieldPatterns: (fieldPattern COMMA)* fieldPattern;

fieldPattern: ID (COLON pattern)?;

patternAlias: ID AT;

applicableExpr:
	parenExpr
	| qualifiedId
	| applicableExpr application;

application: LPAREN RPAREN | LPAREN (expr COMMA)* expr RPAREN;

parenExpr: LPAREN expr RPAREN;

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
LSQUARE: '[';
RSQUARE: ']';

// Reserved operators
EQ: '=';
DOT: '.';
COMMA: ',';
ARROW: '->';
AT: '@';
SEMICOLON: ';';
COLON: ':';
QUESTION: '?';

// Boolean literals
TRUE: 'true';
FALSE: 'false';

// String and char literals
SQUOTE: '\'';
DQUOTE: '"';

LITERAL_CHAR: SQUOTE SINGLE_CHAR SQUOTE;
LITERAL_STRING: DQUOTE STRING_CHAR* DQUOTE;

// Numeric literals
LITERAL_INT: DECIMAL_INTEGER_LITERAL;
LITERAL_FLOAT: DECIMAL_FLOATING_LITERAL;

// Identifiers (normalized)
ID:
	(ID_START ID_CONTINUE* | '_' ID_CONTINUE+) {
		setText(Normalizer.normalize(getText(), Form.NFKC)); 
	};

fragment WS: [ \t\r\n];

fragment ID_START: [\p{XID_Start}];
fragment ID_CONTINUE: [\p{XID_Continue}];

fragment SINGLE_CHAR: ESCAPE_SEQUENCE | ~['\r\n\\];
fragment STRING_CHAR: ESCAPE_SEQUENCE | ~["\r\n\\];

fragment ESCAPE_SEQUENCE:
	'\\b'
	| '\\s'
	| '\\t'
	| '\\n'
	| '\\f'
	| '\\r'
	| '\\"'
	| '\\\''
	| '\\\\'
	| '\\' UNICODE_ESCAPE;

fragment UNICODE_ESCAPE:
	UNICODE_MARKER HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT;

fragment UNICODE_MARKER: 'u';

fragment DECIMAL_INTEGER_LITERAL:
	DECIMAL_NUMERAL INTEGER_SUFFIX?;

fragment DECIMAL_NUMERAL:
	'0'
	| NON_ZERO_DIGIT DIGITS?
	| NON_ZERO_DIGIT UNDERSCORES DIGITS;

fragment DECIMAL_FLOATING_LITERAL:
	DIGITS DOT DIGITS? EXPONENT_PART? FLOAT_SUFFIX?
	| DOT DIGITS EXPONENT_PART? FLOAT_SUFFIX?
	| DIGITS EXPONENT_PART FLOAT_SUFFIX?
	| DIGITS EXPONENT_PART? FLOAT_SUFFIX;

fragment EXPONENT_PART: EXPONENT_INDICATOR SIGNED_INTEGER;
fragment EXPONENT_INDICATOR: [eE];
fragment SIGNED_INTEGER: SIGN? DIGITS;
fragment SIGN: [+-];

fragment DIGITS: DIGIT | DIGIT DIGITS_AND_UNDERSCORES? DIGIT;
fragment DIGITS_AND_UNDERSCORES: DIGIT_OR_UNDERSCORE+;
fragment DIGIT_OR_UNDERSCORE: [_0-9];
fragment UNDERSCORES: '_'+;
fragment DIGIT: [0-9];
fragment HEX_DIGIT: [0-9a-fA-F];
fragment NON_ZERO_DIGIT: [1-9];
fragment INTEGER_SUFFIX: [iIlL];
fragment FLOAT_SUFFIX: [fFdD];
