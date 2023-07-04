parser grammar MinaParser;

options {
    superClass=org.antlr.v4.runtime.Parser;
    tokenVocab=MinaLexer;
}

// Namespaces
namespace: NAMESPACE namespaceId LBRACE importDeclaration* declaration* RBRACE EOF;

// Imports
importDeclaration: IMPORT importSelector;

importSelector:
    namespaceId (DOT ID)?
    | namespaceId DOT LBRACE importee (COMMA importee)* RBRACE;

importee:
    id = ID (AS alias = ID)?;

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
    LSQUARE type (COMMA type)* RSQUARE;

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
namespaceId: (pkg += ID RSLASH)* ns = ID;
qualifiedId: (ns = ID DOT)? id = ID;
