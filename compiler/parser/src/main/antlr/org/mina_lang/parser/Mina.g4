grammar Mina;

options { superClass=org.antlr.v4.runtime.Parser; }

@lexer::header {
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
	moduleId (DOT symbols += ID)?
	| moduleId DOT LBRACE symbols += ID (COMMA symbols += ID)* RBRACE;

// Declarations
declaration: dataDeclaration | letFnDeclaration | letDeclaration;

dataDeclaration:
	DATA ID typeParams? LBRACE dataConstructor* RBRACE;

letFnDeclaration: LET ID typeParams? lambdaParams typeAnnotation? EQ expr;

letDeclaration: LET ID typeAnnotation? EQ expr;

// Data constructors
dataConstructor: CASE ID constructorParams typeAnnotation?;

constructorParams:
	LPAREN RPAREN
	| LPAREN constructorParam (COMMA constructorParam)* RPAREN;

constructorParam: ID typeAnnotation;

// Types
typeAnnotation: COLON type;

type: typeLambda | funType | applicableType;

typeLambda: (typeVar | typeParams) FATARROW type;

typeParams: LSQUARE typeVar (COMMA typeVar)* RSQUARE;

funType: (applicableType | funTypeParams) ARROW type;

funTypeParams: LPAREN RPAREN | LPAREN type (COMMA type)* RPAREN;

applicableType:
	parenType
	| typeReference
	| applicableType typeApplication;

typeApplication:
	LSQUARE typeReference (COMMA typeReference)* RSQUARE;

parenType: LSQUARE type RSQUARE;

typeReference: qualifiedId | typeVar;

typeVar: QUESTION? ID | QUESTION;

// Expressions
expr:
	blockExpr
	| ifExpr
	| lambdaExpr
	| matchExpr
	| literal
	| applicableExpr;

blockExpr: LBRACE letDeclaration* expr RBRACE;

ifExpr: IF expr THEN expr ELSE expr;

lambdaExpr: (ID | lambdaParams) ARROW expr;

lambdaParams:
	LPAREN RPAREN
	| LPAREN lambdaParam (COMMA lambdaParam)* RPAREN;

lambdaParam: ID typeAnnotation?;

matchExpr: MATCH expr WITH LBRACE matchCase* RBRACE;

matchCase: CASE pattern ARROW expr;

pattern: idPattern | literalPattern | constructorPattern;

idPattern: patternAlias? ID;

literalPattern: patternAlias? literal;

constructorPattern:
	patternAlias? qualifiedId LBRACE fieldPatterns? RBRACE;

fieldPatterns: fieldPattern (COMMA fieldPattern)*;

fieldPattern: ID (COLON pattern)?;

patternAlias: ID AT;

applicableExpr:
	parenExpr
	| qualifiedId
	| applicableExpr application;

application: LPAREN RPAREN | LPAREN expr (COMMA expr)* RPAREN;

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
moduleId: (pkg += ID RSLASH)* mod = ID;
qualifiedId: (moduleId DOT)? ID;

WHITESPACE: WS+ -> channel(HIDDEN);

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
FATARROW: '=>';
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

// Generated with [https://util.unicode.org/UnicodeJsps/list-unicodeset.jsp] using
// [\p{XID_Start}&\p{Identifier_Status=Allowed}]
fragment ID_START:
	'A' .. 'Z'
	| 'a' .. 'z'
	| '\u{00C0}' .. '\u{00D6}'
	| '\u{00D8}' .. '\u{00F6}'
	| '\u{00F8}' .. '\u{0131}'
	| '\u{0134}' .. '\u{013E}'
	| '\u{0141}' .. '\u{0148}'
	| '\u{014A}' .. '\u{017E}'
	| '\u{018F}'
	| '\u{01A0}'
	| '\u{01A1}'
	| '\u{01AF}'
	| '\u{01B0}'
	| '\u{01CD}' .. '\u{01DC}'
	| '\u{01DE}' .. '\u{01E3}'
	| '\u{01E6}' .. '\u{01F0}'
	| '\u{01F4}'
	| '\u{01F5}'
	| '\u{01F8}' .. '\u{021B}'
	| '\u{021E}'
	| '\u{021F}'
	| '\u{0226}' .. '\u{0233}'
	| '\u{0259}'
	| '\u{02BB}'
	| '\u{02BC}'
	| '\u{02EC}'
	| '\u{037B}' .. '\u{037D}'
	| '\u{0386}'
	| '\u{0388}' .. '\u{038A}'
	| '\u{038C}'
	| '\u{038E}' .. '\u{03A1}'
	| '\u{03A3}' .. '\u{03CE}'
	| '\u{03FC}' .. '\u{045F}'
	| '\u{048A}' .. '\u{04FF}'
	| '\u{0510}' .. '\u{0529}'
	| '\u{052E}'
	| '\u{052F}'
	| '\u{0531}' .. '\u{0556}'
	| '\u{0559}'
	| '\u{0561}' .. '\u{0586}'
	| '\u{05D0}' .. '\u{05EA}'
	| '\u{05EF}' .. '\u{05F2}'
	| '\u{0620}' .. '\u{063F}'
	| '\u{0641}' .. '\u{064A}'
	| '\u{0671}'
	| '\u{0672}'
	| '\u{0674}'
	| '\u{0679}' .. '\u{068D}'
	| '\u{068F}' .. '\u{06A0}'
	| '\u{06A2}' .. '\u{06D3}'
	| '\u{06D5}'
	| '\u{06E5}'
	| '\u{06E6}'
	| '\u{06EE}'
	| '\u{06EF}'
	| '\u{06FA}' .. '\u{06FC}'
	| '\u{06FF}'
	| '\u{0750}' .. '\u{07A5}'
	| '\u{07B1}'
	| '\u{0870}' .. '\u{0887}'
	| '\u{0889}' .. '\u{088E}'
	| '\u{08A0}' .. '\u{08AC}'
	| '\u{08B2}'
	| '\u{08B5}' .. '\u{08C9}'
	| '\u{0904}' .. '\u{0939}'
	| '\u{093D}'
	| '\u{0950}'
	| '\u{0960}'
	| '\u{0961}'
	| '\u{0971}' .. '\u{0977}'
	| '\u{0979}' .. '\u{097F}'
	| '\u{0985}' .. '\u{098C}'
	| '\u{098F}'
	| '\u{0990}'
	| '\u{0993}' .. '\u{09A8}'
	| '\u{09AA}' .. '\u{09B0}'
	| '\u{09B2}'
	| '\u{09B6}' .. '\u{09B9}'
	| '\u{09BD}'
	| '\u{09CE}'
	| '\u{09E0}'
	| '\u{09E1}'
	| '\u{09F0}'
	| '\u{09F1}'
	| '\u{0A05}' .. '\u{0A0A}'
	| '\u{0A0F}'
	| '\u{0A10}'
	| '\u{0A13}' .. '\u{0A28}'
	| '\u{0A2A}' .. '\u{0A30}'
	| '\u{0A32}'
	| '\u{0A35}'
	| '\u{0A38}'
	| '\u{0A39}'
	| '\u{0A5C}'
	| '\u{0A72}' .. '\u{0A74}'
	| '\u{0A85}' .. '\u{0A8D}'
	| '\u{0A8F}' .. '\u{0A91}'
	| '\u{0A93}' .. '\u{0AA8}'
	| '\u{0AAA}' .. '\u{0AB0}'
	| '\u{0AB2}'
	| '\u{0AB3}'
	| '\u{0AB5}' .. '\u{0AB9}'
	| '\u{0ABD}'
	| '\u{0AD0}'
	| '\u{0AE0}'
	| '\u{0AE1}'
	| '\u{0B05}' .. '\u{0B0C}'
	| '\u{0B0F}'
	| '\u{0B10}'
	| '\u{0B13}' .. '\u{0B28}'
	| '\u{0B2A}' .. '\u{0B30}'
	| '\u{0B32}'
	| '\u{0B33}'
	| '\u{0B35}' .. '\u{0B39}'
	| '\u{0B3D}'
	| '\u{0B5F}' .. '\u{0B61}'
	| '\u{0B71}'
	| '\u{0B83}'
	| '\u{0B85}' .. '\u{0B8A}'
	| '\u{0B8E}' .. '\u{0B90}'
	| '\u{0B92}' .. '\u{0B95}'
	| '\u{0B99}'
	| '\u{0B9A}'
	| '\u{0B9C}'
	| '\u{0B9E}'
	| '\u{0B9F}'
	| '\u{0BA3}'
	| '\u{0BA4}'
	| '\u{0BA8}' .. '\u{0BAA}'
	| '\u{0BAE}' .. '\u{0BB9}'
	| '\u{0BD0}'
	| '\u{0C05}' .. '\u{0C0C}'
	| '\u{0C0E}' .. '\u{0C10}'
	| '\u{0C12}' .. '\u{0C28}'
	| '\u{0C2A}' .. '\u{0C33}'
	| '\u{0C35}' .. '\u{0C39}'
	| '\u{0C3D}'
	| '\u{0C5D}'
	| '\u{0C60}'
	| '\u{0C61}'
	| '\u{0C80}'
	| '\u{0C85}' .. '\u{0C8C}'
	| '\u{0C8E}' .. '\u{0C90}'
	| '\u{0C92}' .. '\u{0CA8}'
	| '\u{0CAA}' .. '\u{0CB3}'
	| '\u{0CB5}' .. '\u{0CB9}'
	| '\u{0CBD}'
	| '\u{0CDD}'
	| '\u{0CE0}'
	| '\u{0CE1}'
	| '\u{0CF1}'
	| '\u{0CF2}'
	| '\u{0D05}' .. '\u{0D0C}'
	| '\u{0D0E}' .. '\u{0D10}'
	| '\u{0D12}' .. '\u{0D3A}'
	| '\u{0D3D}'
	| '\u{0D4E}'
	| '\u{0D54}' .. '\u{0D56}'
	| '\u{0D60}'
	| '\u{0D61}'
	| '\u{0D7A}' .. '\u{0D7F}'
	| '\u{0D85}' .. '\u{0D8E}'
	| '\u{0D91}' .. '\u{0D96}'
	| '\u{0D9A}' .. '\u{0DA5}'
	| '\u{0DA7}' .. '\u{0DB1}'
	| '\u{0DB3}' .. '\u{0DBB}'
	| '\u{0DBD}'
	| '\u{0DC0}' .. '\u{0DC6}'
	| '\u{0E01}' .. '\u{0E30}'
	| '\u{0E32}'
	| '\u{0E40}' .. '\u{0E46}'
	| '\u{0E81}'
	| '\u{0E82}'
	| '\u{0E84}'
	| '\u{0E86}' .. '\u{0E8A}'
	| '\u{0E8C}' .. '\u{0EA3}'
	| '\u{0EA5}'
	| '\u{0EA7}' .. '\u{0EB0}'
	| '\u{0EB2}'
	| '\u{0EBD}'
	| '\u{0EC0}' .. '\u{0EC4}'
	| '\u{0EC6}'
	| '\u{0EDE}'
	| '\u{0EDF}'
	| '\u{0F00}'
	| '\u{0F40}' .. '\u{0F42}'
	| '\u{0F44}' .. '\u{0F47}'
	| '\u{0F49}' .. '\u{0F4C}'
	| '\u{0F4E}' .. '\u{0F51}'
	| '\u{0F53}' .. '\u{0F56}'
	| '\u{0F58}' .. '\u{0F5B}'
	| '\u{0F5D}' .. '\u{0F68}'
	| '\u{0F6A}' .. '\u{0F6C}'
	| '\u{0F88}' .. '\u{0F8C}'
	| '\u{1000}' .. '\u{102A}'
	| '\u{103F}'
	| '\u{1050}' .. '\u{1055}'
	| '\u{105A}' .. '\u{105D}'
	| '\u{1061}'
	| '\u{1065}'
	| '\u{1066}'
	| '\u{106E}' .. '\u{1070}'
	| '\u{1075}' .. '\u{1081}'
	| '\u{108E}'
	| '\u{10C7}'
	| '\u{10CD}'
	| '\u{10D0}' .. '\u{10F0}'
	| '\u{10F7}' .. '\u{10FA}'
	| '\u{10FD}' .. '\u{10FF}'
	| '\u{1200}' .. '\u{1248}'
	| '\u{124A}' .. '\u{124D}'
	| '\u{1250}' .. '\u{1256}'
	| '\u{1258}'
	| '\u{125A}' .. '\u{125D}'
	| '\u{1260}' .. '\u{1288}'
	| '\u{128A}' .. '\u{128D}'
	| '\u{1290}' .. '\u{12B0}'
	| '\u{12B2}' .. '\u{12B5}'
	| '\u{12B8}' .. '\u{12BE}'
	| '\u{12C0}'
	| '\u{12C2}' .. '\u{12C5}'
	| '\u{12C8}' .. '\u{12D6}'
	| '\u{12D8}' .. '\u{1310}'
	| '\u{1312}' .. '\u{1315}'
	| '\u{1318}' .. '\u{135A}'
	| '\u{1380}' .. '\u{138F}'
	| '\u{1780}' .. '\u{17A2}'
	| '\u{17A5}' .. '\u{17A7}'
	| '\u{17A9}' .. '\u{17B3}'
	| '\u{17D7}'
	| '\u{17DC}'
	| '\u{1C90}' .. '\u{1CBA}'
	| '\u{1CBD}' .. '\u{1CBF}'
	| '\u{1E00}' .. '\u{1E99}'
	| '\u{1E9E}'
	| '\u{1EA0}' .. '\u{1EF9}'
	| '\u{1F00}' .. '\u{1F15}'
	| '\u{1F18}' .. '\u{1F1D}'
	| '\u{1F20}' .. '\u{1F45}'
	| '\u{1F48}' .. '\u{1F4D}'
	| '\u{1F50}' .. '\u{1F57}'
	| '\u{1F59}'
	| '\u{1F5B}'
	| '\u{1F5D}'
	| '\u{1F5F}' .. '\u{1F70}'
	| '\u{1F72}'
	| '\u{1F74}'
	| '\u{1F76}'
	| '\u{1F78}'
	| '\u{1F7A}'
	| '\u{1F7C}'
	| '\u{1F80}' .. '\u{1FB4}'
	| '\u{1FB6}' .. '\u{1FBA}'
	| '\u{1FBC}'
	| '\u{1FC2}' .. '\u{1FC4}'
	| '\u{1FC6}' .. '\u{1FC8}'
	| '\u{1FCA}'
	| '\u{1FCC}'
	| '\u{1FD0}' .. '\u{1FD2}'
	| '\u{1FD6}' .. '\u{1FDA}'
	| '\u{1FE0}' .. '\u{1FE2}'
	| '\u{1FE4}' .. '\u{1FEA}'
	| '\u{1FEC}'
	| '\u{1FF2}' .. '\u{1FF4}'
	| '\u{1FF6}' .. '\u{1FF8}'
	| '\u{1FFA}'
	| '\u{1FFC}'
	| '\u{2D27}'
	| '\u{2D2D}'
	| '\u{2D80}' .. '\u{2D96}'
	| '\u{2DA0}' .. '\u{2DA6}'
	| '\u{2DA8}' .. '\u{2DAE}'
	| '\u{2DB0}' .. '\u{2DB6}'
	| '\u{2DB8}' .. '\u{2DBE}'
	| '\u{2DC0}' .. '\u{2DC6}'
	| '\u{2DC8}' .. '\u{2DCE}'
	| '\u{2DD0}' .. '\u{2DD6}'
	| '\u{2DD8}' .. '\u{2DDE}'
	| '\u{3005}' .. '\u{3007}'
	| '\u{3041}' .. '\u{3096}'
	| '\u{309D}'
	| '\u{309E}'
	| '\u{30A1}' .. '\u{30FA}'
	| '\u{30FC}' .. '\u{30FE}'
	| '\u{3105}' .. '\u{312D}'
	| '\u{312F}'
	| '\u{31A0}' .. '\u{31BF}'
	| '\u{3400}' .. '\u{4DBF}'
	| '\u{4E00}' .. '\u{9FFF}'
	| '\u{A67F}'
	| '\u{A717}' .. '\u{A71F}'
	| '\u{A788}'
	| '\u{A78D}'
	| '\u{A792}'
	| '\u{A793}'
	| '\u{A7AA}'
	| '\u{A7AE}'
	| '\u{A7B8}'
	| '\u{A7B9}'
	| '\u{A7C0}' .. '\u{A7CA}'
	| '\u{A7D0}'
	| '\u{A7D1}'
	| '\u{A7D3}'
	| '\u{A7D5}' .. '\u{A7D9}'
	| '\u{A9E7}' .. '\u{A9EF}'
	| '\u{A9FA}' .. '\u{A9FE}'
	| '\u{AA60}' .. '\u{AA76}'
	| '\u{AA7A}'
	| '\u{AA7E}'
	| '\u{AA7F}'
	| '\u{AB01}' .. '\u{AB06}'
	| '\u{AB09}' .. '\u{AB0E}'
	| '\u{AB11}' .. '\u{AB16}'
	| '\u{AB20}' .. '\u{AB26}'
	| '\u{AB28}' .. '\u{AB2E}'
	| '\u{AB66}'
	| '\u{AB67}'
	| '\u{AC00}' .. '\u{D7A3}'
	| '\u{FA0E}'
	| '\u{FA0F}'
	| '\u{FA11}'
	| '\u{FA13}'
	| '\u{FA14}'
	| '\u{FA1F}'
	| '\u{FA21}'
	| '\u{FA23}'
	| '\u{FA24}'
	| '\u{FA27}' .. '\u{FA29}'
	| '\u{1B11F}' .. '\u{1B122}'
	| '\u{1B150}' .. '\u{1B152}'
	| '\u{1B164}' .. '\u{1B167}'
	| '\u{1DF00}' .. '\u{1DF1E}'
	| '\u{1E7E0}' .. '\u{1E7E6}'
	| '\u{1E7E8}' .. '\u{1E7EB}'
	| '\u{1E7ED}'
	| '\u{1E7EE}'
	| '\u{1E7F0}' .. '\u{1E7FE}'
	| '\u{20000}' .. '\u{2A6DF}'
	| '\u{2A700}' .. '\u{2B738}'
	| '\u{2B740}' .. '\u{2B81D}'
	| '\u{2B820}' .. '\u{2CEA1}'
	| '\u{2CEB0}' .. '\u{2EBE0}'
	| '\u{30000}' .. '\u{3134A}';

// Generated with [https://util.unicode.org/UnicodeJsps/list-unicodeset.jsp] using
// [\p{XID_Continue}&\p{Identifier_Status=Allowed}]
fragment ID_CONTINUE:
	'0' .. '9'
	| 'A' .. 'Z'
	| 'a' .. 'z'
	| '\u{00B7}'
	| '\u{00C0}' .. '\u{00D6}'
	| '\u{00D8}' .. '\u{00F6}'
	| '\u{00F8}' .. '\u{0131}'
	| '\u{0134}' .. '\u{013E}'
	| '\u{0141}' .. '\u{0148}'
	| '\u{014A}' .. '\u{017E}'
	| '\u{018F}'
	| '\u{01A0}'
	| '\u{01A1}'
	| '\u{01AF}'
	| '\u{01B0}'
	| '\u{01CD}' .. '\u{01DC}'
	| '\u{01DE}' .. '\u{01E3}'
	| '\u{01E6}' .. '\u{01F0}'
	| '\u{01F4}'
	| '\u{01F5}'
	| '\u{01F8}' .. '\u{021B}'
	| '\u{021E}'
	| '\u{021F}'
	| '\u{0226}' .. '\u{0233}'
	| '\u{0259}'
	| '\u{02BB}'
	| '\u{02BC}'
	| '\u{02EC}'
	| '\u{0300}' .. '\u{0304}'
	| '\u{0306}' .. '\u{030C}'
	| '\u{030F}' .. '\u{0311}'
	| '\u{0313}'
	| '\u{0314}'
	| '\u{031B}'
	| '\u{0323}' .. '\u{0328}'
	| '\u{032D}'
	| '\u{032E}'
	| '\u{0330}'
	| '\u{0331}'
	| '\u{0335}'
	| '\u{0338}'
	| '\u{0339}'
	| '\u{0342}'
	| '\u{0345}'
	| '\u{037B}' .. '\u{037D}'
	| '\u{0386}'
	| '\u{0388}' .. '\u{038A}'
	| '\u{038C}'
	| '\u{038E}' .. '\u{03A1}'
	| '\u{03A3}' .. '\u{03CE}'
	| '\u{03FC}' .. '\u{045F}'
	| '\u{048A}' .. '\u{04FF}'
	| '\u{0510}' .. '\u{0529}'
	| '\u{052E}'
	| '\u{052F}'
	| '\u{0531}' .. '\u{0556}'
	| '\u{0559}'
	| '\u{0561}' .. '\u{0586}'
	| '\u{05B4}'
	| '\u{05D0}' .. '\u{05EA}'
	| '\u{05EF}' .. '\u{05F2}'
	| '\u{0620}' .. '\u{063F}'
	| '\u{0641}' .. '\u{0655}'
	| '\u{0660}' .. '\u{0669}'
	| '\u{0670}' .. '\u{0672}'
	| '\u{0674}'
	| '\u{0679}' .. '\u{068D}'
	| '\u{068F}' .. '\u{06A0}'
	| '\u{06A2}' .. '\u{06D3}'
	| '\u{06D5}'
	| '\u{06E5}'
	| '\u{06E6}'
	| '\u{06EE}' .. '\u{06FC}'
	| '\u{06FF}'
	| '\u{0750}' .. '\u{07B1}'
	| '\u{0870}' .. '\u{0887}'
	| '\u{0889}' .. '\u{088E}'
	| '\u{08A0}' .. '\u{08AC}'
	| '\u{08B2}'
	| '\u{08B5}' .. '\u{08C9}'
	| '\u{0901}' .. '\u{094D}'
	| '\u{094F}'
	| '\u{0950}'
	| '\u{0956}'
	| '\u{0957}'
	| '\u{0960}' .. '\u{0963}'
	| '\u{0966}' .. '\u{096F}'
	| '\u{0971}' .. '\u{0977}'
	| '\u{0979}' .. '\u{097F}'
	| '\u{0981}' .. '\u{0983}'
	| '\u{0985}' .. '\u{098C}'
	| '\u{098F}'
	| '\u{0990}'
	| '\u{0993}' .. '\u{09A8}'
	| '\u{09AA}' .. '\u{09B0}'
	| '\u{09B2}'
	| '\u{09B6}' .. '\u{09B9}'
	| '\u{09BC}' .. '\u{09C4}'
	| '\u{09C7}'
	| '\u{09C8}'
	| '\u{09CB}' .. '\u{09CE}'
	| '\u{09D7}'
	| '\u{09E0}' .. '\u{09E3}'
	| '\u{09E6}' .. '\u{09F1}'
	| '\u{09FE}'
	| '\u{0A01}' .. '\u{0A03}'
	| '\u{0A05}' .. '\u{0A0A}'
	| '\u{0A0F}'
	| '\u{0A10}'
	| '\u{0A13}' .. '\u{0A28}'
	| '\u{0A2A}' .. '\u{0A30}'
	| '\u{0A32}'
	| '\u{0A35}'
	| '\u{0A38}'
	| '\u{0A39}'
	| '\u{0A3C}'
	| '\u{0A3E}' .. '\u{0A42}'
	| '\u{0A47}'
	| '\u{0A48}'
	| '\u{0A4B}' .. '\u{0A4D}'
	| '\u{0A5C}'
	| '\u{0A66}' .. '\u{0A74}'
	| '\u{0A81}' .. '\u{0A83}'
	| '\u{0A85}' .. '\u{0A8D}'
	| '\u{0A8F}' .. '\u{0A91}'
	| '\u{0A93}' .. '\u{0AA8}'
	| '\u{0AAA}' .. '\u{0AB0}'
	| '\u{0AB2}'
	| '\u{0AB3}'
	| '\u{0AB5}' .. '\u{0AB9}'
	| '\u{0ABC}' .. '\u{0AC5}'
	| '\u{0AC7}' .. '\u{0AC9}'
	| '\u{0ACB}' .. '\u{0ACD}'
	| '\u{0AD0}'
	| '\u{0AE0}' .. '\u{0AE3}'
	| '\u{0AE6}' .. '\u{0AEF}'
	| '\u{0AFA}' .. '\u{0AFF}'
	| '\u{0B01}' .. '\u{0B03}'
	| '\u{0B05}' .. '\u{0B0C}'
	| '\u{0B0F}'
	| '\u{0B10}'
	| '\u{0B13}' .. '\u{0B28}'
	| '\u{0B2A}' .. '\u{0B30}'
	| '\u{0B32}'
	| '\u{0B33}'
	| '\u{0B35}' .. '\u{0B39}'
	| '\u{0B3C}' .. '\u{0B43}'
	| '\u{0B47}'
	| '\u{0B48}'
	| '\u{0B4B}' .. '\u{0B4D}'
	| '\u{0B55}' .. '\u{0B57}'
	| '\u{0B5F}' .. '\u{0B61}'
	| '\u{0B66}' .. '\u{0B6F}'
	| '\u{0B71}'
	| '\u{0B82}'
	| '\u{0B83}'
	| '\u{0B85}' .. '\u{0B8A}'
	| '\u{0B8E}' .. '\u{0B90}'
	| '\u{0B92}' .. '\u{0B95}'
	| '\u{0B99}'
	| '\u{0B9A}'
	| '\u{0B9C}'
	| '\u{0B9E}'
	| '\u{0B9F}'
	| '\u{0BA3}'
	| '\u{0BA4}'
	| '\u{0BA8}' .. '\u{0BAA}'
	| '\u{0BAE}' .. '\u{0BB9}'
	| '\u{0BBE}' .. '\u{0BC2}'
	| '\u{0BC6}' .. '\u{0BC8}'
	| '\u{0BCA}' .. '\u{0BCD}'
	| '\u{0BD0}'
	| '\u{0BD7}'
	| '\u{0BE6}' .. '\u{0BEF}'
	| '\u{0C01}' .. '\u{0C0C}'
	| '\u{0C0E}' .. '\u{0C10}'
	| '\u{0C12}' .. '\u{0C28}'
	| '\u{0C2A}' .. '\u{0C33}'
	| '\u{0C35}' .. '\u{0C39}'
	| '\u{0C3C}' .. '\u{0C44}'
	| '\u{0C46}' .. '\u{0C48}'
	| '\u{0C4A}' .. '\u{0C4D}'
	| '\u{0C55}'
	| '\u{0C56}'
	| '\u{0C5D}'
	| '\u{0C60}'
	| '\u{0C61}'
	| '\u{0C66}' .. '\u{0C6F}'
	| '\u{0C80}'
	| '\u{0C82}'
	| '\u{0C83}'
	| '\u{0C85}' .. '\u{0C8C}'
	| '\u{0C8E}' .. '\u{0C90}'
	| '\u{0C92}' .. '\u{0CA8}'
	| '\u{0CAA}' .. '\u{0CB3}'
	| '\u{0CB5}' .. '\u{0CB9}'
	| '\u{0CBC}' .. '\u{0CC4}'
	| '\u{0CC6}' .. '\u{0CC8}'
	| '\u{0CCA}' .. '\u{0CCD}'
	| '\u{0CD5}'
	| '\u{0CD6}'
	| '\u{0CDD}'
	| '\u{0CE0}' .. '\u{0CE3}'
	| '\u{0CE6}' .. '\u{0CEF}'
	| '\u{0CF1}'
	| '\u{0CF2}'
	| '\u{0D00}'
	| '\u{0D02}'
	| '\u{0D03}'
	| '\u{0D05}' .. '\u{0D0C}'
	| '\u{0D0E}' .. '\u{0D10}'
	| '\u{0D12}' .. '\u{0D3A}'
	| '\u{0D3D}' .. '\u{0D43}'
	| '\u{0D46}' .. '\u{0D48}'
	| '\u{0D4A}' .. '\u{0D4E}'
	| '\u{0D54}' .. '\u{0D57}'
	| '\u{0D60}'
	| '\u{0D61}'
	| '\u{0D66}' .. '\u{0D6F}'
	| '\u{0D7A}' .. '\u{0D7F}'
	| '\u{0D82}'
	| '\u{0D83}'
	| '\u{0D85}' .. '\u{0D8E}'
	| '\u{0D91}' .. '\u{0D96}'
	| '\u{0D9A}' .. '\u{0DA5}'
	| '\u{0DA7}' .. '\u{0DB1}'
	| '\u{0DB3}' .. '\u{0DBB}'
	| '\u{0DBD}'
	| '\u{0DC0}' .. '\u{0DC6}'
	| '\u{0DCA}'
	| '\u{0DCF}' .. '\u{0DD4}'
	| '\u{0DD6}'
	| '\u{0DD8}' .. '\u{0DDE}'
	| '\u{0DF2}'
	| '\u{0E01}' .. '\u{0E32}'
	| '\u{0E34}' .. '\u{0E3A}'
	| '\u{0E40}' .. '\u{0E4E}'
	| '\u{0E50}' .. '\u{0E59}'
	| '\u{0E81}'
	| '\u{0E82}'
	| '\u{0E84}'
	| '\u{0E86}' .. '\u{0E8A}'
	| '\u{0E8C}' .. '\u{0EA3}'
	| '\u{0EA5}'
	| '\u{0EA7}' .. '\u{0EB2}'
	| '\u{0EB4}' .. '\u{0EBD}'
	| '\u{0EC0}' .. '\u{0EC4}'
	| '\u{0EC6}'
	| '\u{0EC8}' .. '\u{0ECD}'
	| '\u{0ED0}' .. '\u{0ED9}'
	| '\u{0EDE}'
	| '\u{0EDF}'
	| '\u{0F00}'
	| '\u{0F20}' .. '\u{0F29}'
	| '\u{0F35}'
	| '\u{0F37}'
	| '\u{0F3E}' .. '\u{0F42}'
	| '\u{0F44}' .. '\u{0F47}'
	| '\u{0F49}' .. '\u{0F4C}'
	| '\u{0F4E}' .. '\u{0F51}'
	| '\u{0F53}' .. '\u{0F56}'
	| '\u{0F58}' .. '\u{0F5B}'
	| '\u{0F5D}' .. '\u{0F68}'
	| '\u{0F6A}' .. '\u{0F6C}'
	| '\u{0F71}'
	| '\u{0F72}'
	| '\u{0F74}'
	| '\u{0F7A}' .. '\u{0F80}'
	| '\u{0F82}' .. '\u{0F84}'
	| '\u{0F86}' .. '\u{0F92}'
	| '\u{0F94}' .. '\u{0F97}'
	| '\u{0F99}' .. '\u{0F9C}'
	| '\u{0F9E}' .. '\u{0FA1}'
	| '\u{0FA3}' .. '\u{0FA6}'
	| '\u{0FA8}' .. '\u{0FAB}'
	| '\u{0FAD}' .. '\u{0FB8}'
	| '\u{0FBA}' .. '\u{0FBC}'
	| '\u{0FC6}'
	| '\u{1000}' .. '\u{1049}'
	| '\u{1050}' .. '\u{109D}'
	| '\u{10C7}'
	| '\u{10CD}'
	| '\u{10D0}' .. '\u{10F0}'
	| '\u{10F7}' .. '\u{10FA}'
	| '\u{10FD}' .. '\u{10FF}'
	| '\u{1200}' .. '\u{1248}'
	| '\u{124A}' .. '\u{124D}'
	| '\u{1250}' .. '\u{1256}'
	| '\u{1258}'
	| '\u{125A}' .. '\u{125D}'
	| '\u{1260}' .. '\u{1288}'
	| '\u{128A}' .. '\u{128D}'
	| '\u{1290}' .. '\u{12B0}'
	| '\u{12B2}' .. '\u{12B5}'
	| '\u{12B8}' .. '\u{12BE}'
	| '\u{12C0}'
	| '\u{12C2}' .. '\u{12C5}'
	| '\u{12C8}' .. '\u{12D6}'
	| '\u{12D8}' .. '\u{1310}'
	| '\u{1312}' .. '\u{1315}'
	| '\u{1318}' .. '\u{135A}'
	| '\u{135D}' .. '\u{135F}'
	| '\u{1380}' .. '\u{138F}'
	| '\u{1780}' .. '\u{17A2}'
	| '\u{17A5}' .. '\u{17A7}'
	| '\u{17A9}' .. '\u{17B3}'
	| '\u{17B6}' .. '\u{17CA}'
	| '\u{17D2}'
	| '\u{17D7}'
	| '\u{17DC}'
	| '\u{17E0}' .. '\u{17E9}'
	| '\u{1C90}' .. '\u{1CBA}'
	| '\u{1CBD}' .. '\u{1CBF}'
	| '\u{1E00}' .. '\u{1E99}'
	| '\u{1E9E}'
	| '\u{1EA0}' .. '\u{1EF9}'
	| '\u{1F00}' .. '\u{1F15}'
	| '\u{1F18}' .. '\u{1F1D}'
	| '\u{1F20}' .. '\u{1F45}'
	| '\u{1F48}' .. '\u{1F4D}'
	| '\u{1F50}' .. '\u{1F57}'
	| '\u{1F59}'
	| '\u{1F5B}'
	| '\u{1F5D}'
	| '\u{1F5F}' .. '\u{1F70}'
	| '\u{1F72}'
	| '\u{1F74}'
	| '\u{1F76}'
	| '\u{1F78}'
	| '\u{1F7A}'
	| '\u{1F7C}'
	| '\u{1F80}' .. '\u{1FB4}'
	| '\u{1FB6}' .. '\u{1FBA}'
	| '\u{1FBC}'
	| '\u{1FC2}' .. '\u{1FC4}'
	| '\u{1FC6}' .. '\u{1FC8}'
	| '\u{1FCA}'
	| '\u{1FCC}'
	| '\u{1FD0}' .. '\u{1FD2}'
	| '\u{1FD6}' .. '\u{1FDA}'
	| '\u{1FE0}' .. '\u{1FE2}'
	| '\u{1FE4}' .. '\u{1FEA}'
	| '\u{1FEC}'
	| '\u{1FF2}' .. '\u{1FF4}'
	| '\u{1FF6}' .. '\u{1FF8}'
	| '\u{1FFA}'
	| '\u{1FFC}'
	| '\u{2D27}'
	| '\u{2D2D}'
	| '\u{2D80}' .. '\u{2D96}'
	| '\u{2DA0}' .. '\u{2DA6}'
	| '\u{2DA8}' .. '\u{2DAE}'
	| '\u{2DB0}' .. '\u{2DB6}'
	| '\u{2DB8}' .. '\u{2DBE}'
	| '\u{2DC0}' .. '\u{2DC6}'
	| '\u{2DC8}' .. '\u{2DCE}'
	| '\u{2DD0}' .. '\u{2DD6}'
	| '\u{2DD8}' .. '\u{2DDE}'
	| '\u{3005}' .. '\u{3007}'
	| '\u{3041}' .. '\u{3096}'
	| '\u{3099}'
	| '\u{309A}'
	| '\u{309D}'
	| '\u{309E}'
	| '\u{30A1}' .. '\u{30FA}'
	| '\u{30FC}' .. '\u{30FE}'
	| '\u{3105}' .. '\u{312D}'
	| '\u{312F}'
	| '\u{31A0}' .. '\u{31BF}'
	| '\u{3400}' .. '\u{4DBF}'
	| '\u{4E00}' .. '\u{9FFF}'
	| '\u{A67F}'
	| '\u{A717}' .. '\u{A71F}'
	| '\u{A788}'
	| '\u{A78D}'
	| '\u{A792}'
	| '\u{A793}'
	| '\u{A7AA}'
	| '\u{A7AE}'
	| '\u{A7B8}'
	| '\u{A7B9}'
	| '\u{A7C0}' .. '\u{A7CA}'
	| '\u{A7D0}'
	| '\u{A7D1}'
	| '\u{A7D3}'
	| '\u{A7D5}' .. '\u{A7D9}'
	| '\u{A9E7}' .. '\u{A9FE}'
	| '\u{AA60}' .. '\u{AA76}'
	| '\u{AA7A}' .. '\u{AA7F}'
	| '\u{AB01}' .. '\u{AB06}'
	| '\u{AB09}' .. '\u{AB0E}'
	| '\u{AB11}' .. '\u{AB16}'
	| '\u{AB20}' .. '\u{AB26}'
	| '\u{AB28}' .. '\u{AB2E}'
	| '\u{AB66}'
	| '\u{AB67}'
	| '\u{AC00}' .. '\u{D7A3}'
	| '\u{FA0E}'
	| '\u{FA0F}'
	| '\u{FA11}'
	| '\u{FA13}'
	| '\u{FA14}'
	| '\u{FA1F}'
	| '\u{FA21}'
	| '\u{FA23}'
	| '\u{FA24}'
	| '\u{FA27}' .. '\u{FA29}'
	| '\u{11301}'
	| '\u{11303}'
	| '\u{1133B}'
	| '\u{1133C}'
	| '\u{16FF0}'
	| '\u{16FF1}'
	| '\u{1B11F}' .. '\u{1B122}'
	| '\u{1B150}' .. '\u{1B152}'
	| '\u{1B164}' .. '\u{1B167}'
	| '\u{1DF00}' .. '\u{1DF1E}'
	| '\u{1E7E0}' .. '\u{1E7E6}'
	| '\u{1E7E8}' .. '\u{1E7EB}'
	| '\u{1E7ED}'
	| '\u{1E7EE}'
	| '\u{1E7F0}' .. '\u{1E7FE}'
	| '\u{20000}' .. '\u{2A6DF}'
	| '\u{2A700}' .. '\u{2B738}'
	| '\u{2B740}' .. '\u{2B81D}'
	| '\u{2B820}' .. '\u{2CEA1}'
	| '\u{2CEB0}' .. '\u{2EBE0}'
	| '\u{30000}' .. '\u{3134A}';
