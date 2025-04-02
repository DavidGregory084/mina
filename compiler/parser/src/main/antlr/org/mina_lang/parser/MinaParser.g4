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

/** Namespaces:
  * namespace Mina/Parser/Example {
  *   import Mina/Parser/Example2.{id as id2}
  *   data Void {}
  *   let one: Int = id2(1)
  * }
  */
namespace: NAMESPACE namespaceId LBRACE importDeclaration* declaration* RBRACE EOF;

/** Imports:
  * import Mina/Parser/Example1
  * import Mina/Parser/Example2 as Ex2
  * import Mina/Parser/Example1.id
  * import Mina/Parser/Example2.{id as id2}
  * import Mina/Parser/Example2.{const as const2, compose}
  */
importDeclaration: IMPORT namespaceId (AS alias = ID | DOT importedSymbols)?;

importedSymbols:
    id = ID
    | LBRACE importee (COMMA importee)* COMMA? RBRACE
    ;

importee: id = ID (AS alias = ID)?;

/** Declarations */
declaration: dataDeclaration | letDeclaration;

/** Let declarations:
 *  let id: [A] { A -> A } = a -> a
 *  let id[A](a: A): A = a
 */
letDeclaration: LET ID (typeParams? lambdaParams)? typeAnnotation? EQUAL expr;

/** Data declarations:
 *  data List[A] {
 *    case Cons(head: A, tail: List[A])
 *    case Nil()
 *  }
 */
dataDeclaration: DATA ID typeParams? LBRACE dataConstructor* RBRACE;

dataConstructor: CASE ID constructorParams typeAnnotation?;

constructorParams: LPAREN (constructorParam (COMMA constructorParam)* COMMA?)? RPAREN;

constructorParam: ID typeAnnotation;

/** Type annotation:
 *  : List[A]
 */
typeAnnotation: COLON type;

/** Types */
type:
    // Factor out the common prefix between e.g. A and A -> A by
    // parsing unary function types via an optional suffix
    (applicableType | quantifiedType) (ARROW type)?
    // Multi-argument and nullary function types (A, B) -> A and () -> A
    | funTypeParams ARROW type
    ;

/** Quantified types:
 *  [A] { A -> A }
 *  [?A] { Pair[?A, ?A -> String] }
 */
quantifiedType: typeParams LBRACE type RBRACE;

typeParams: LSQUARE typeVar (COMMA typeVar)* COMMA? RSQUARE;

funTypeParams: LPAREN (type (COMMA type)* COMMA?)? RPAREN;

/** Types that can be applied with type parameters:
 *  List[A]
 *  F[G[A]]
 */
applicableType:
    typeReference
    | parenType
    | applicableType typeApplication
    ;

typeApplication: LSQUARE type (COMMA type)* COMMA? RSQUARE;

/** Grouping for types, useful for curried functions e.g.
 *  [A -> B] -> A -> B
 */
parenType: LSQUARE type RSQUARE;

/** References to known types, e.g.
 *  Int, List, A, ?A
 *
 * Note that `qualifiedId` subsumes `forAllVar`.
 */
typeReference: qualifiedId | existsVar;

/** Type variables: A, ?A */
typeVar: forAllVar | existsVar;

forAllVar: ID;

existsVar: QUESTION ID?;

/** Expressions */
expr:
    applicableExpr
    | ifExpr
    | matchExpr
    | lambdaExpr
    ;

/** Grouping for expressions: a && (b || c) */
parenExpr: LPAREN expr RPAREN;

/** Blocks:
 *  {
 *    let a = 1
 *    let b = 2
 *    a + b
 *  }
 */
blockExpr: LBRACE localLet* expr? RBRACE;

/** Local let bindings - this rule currently disallows the function syntax for let bindings */
localLet: LET ID typeAnnotation? EQUAL expr;

/** If expressions:
 *
 *  if true then 1 else 2
 *
 *  if a && b then {
 *    1
 *  } else {
 *    2
 *  }
 */
ifExpr: IF expr THEN expr ELSE expr;

/** Lambda expressions:
 *  a -> a
 *  (a) -> a
 *  () -> 1
 *  (l: Int, r: Int) -> l + r
 */
lambdaExpr: (ID | lambdaParams) ARROW expr;

lambdaParams: LPAREN (lambdaParam (COMMA lambdaParam)* COMMA?)? RPAREN;

lambdaParam: ID typeAnnotation?;

/** Match expressions:
 *  match list with {
 *    case Cons { head: last, tail: Nil{} } -> Some(last)
 *    case Cons { tail: rest } -> lastOption(rest)
 *    case Nil {} -> None()
 *  }
 */
matchExpr: MATCH expr WITH LBRACE matchCase* RBRACE;

matchCase: CASE pattern ARROW expr;

/** Patterns */
pattern: idPattern | literalPattern | constructorPattern;

/** Identifier and alias patterns
 *  head
 *  rest @ Cons {}
 */
idPattern: ID (AT pattern)?;

/** Literal patterns:
 *  1
 *  "hello"
 */
literalPattern: literal;

/** Constructor patterns
 *  Cons { head, tail: Nil {} }
 *  Cons { head: first }
 */
constructorPattern: qualifiedId LBRACE fieldPatterns? RBRACE;

fieldPatterns: fieldPattern (COMMA fieldPattern)* COMMA?;

fieldPattern: ID (COLON pattern)?;

/** Expressions that can be applied with arguments and operators.
 *  This rule encodes the operator precedence hierarchy.
 */
applicableExpr:
    // Identifiers: x
    id = ID
    // Literals: 1, 'a', "hello"
    | literal
    // Parentheses
    | parenExpr
    // Blocks
    | blockExpr
    // Member selection: x.id
    | receiver = applicableExpr DOT selection = ID
    // Function application: id(x)
    | function = applicableExpr application
    // Prefix operators: -x, !x, ~x
    | operator = (MINUS | EXCLAMATION | TILDE) unaryOperand = applicableExpr
    // Multiplicative arithmetic operators: a * b, a / b, a % b
    | leftOperand = applicableExpr operator = (ASTERISK | RSLASH | PERCENT) rightOperand = applicableExpr
    // Additive arithmetic operators: a + b, a - b
    | leftOperand = applicableExpr operator = (PLUS | MINUS) rightOperand = applicableExpr
    // Bitwise shift operators: a << b, a >> b, a >>> b
    | leftOperand = applicableExpr operator = (LEFT_SHIFT | RIGHT_SHIFT | UNSIGNED_RIGHT_SHIFT) rightOperand = applicableExpr
    // Bitwise and: a & b
    | leftOperand = applicableExpr operator = AMPERSAND rightOperand = applicableExpr
    // Bitwise or and xor: a ^ b, a | b
    | leftOperand = applicableExpr operator = (CARET | PIPE) rightOperand = applicableExpr
    // Relational operators: a < b, a <= b, a > b, a >= b
    | leftOperand = applicableExpr operator = (LESS_THAN | LESS_THAN_EQUAL | GREATER_THAN | GREATER_THAN_EQUAL) rightOperand = applicableExpr
    // Equality operators: a == b, a != b
    | leftOperand = applicableExpr operator = (DOUBLE_EQUAL | NOT_EQUAL) rightOperand = applicableExpr
    // Logical operators: a && b, a || b
    | leftOperand = applicableExpr operator = DOUBLE_AMPERSAND rightOperand = applicableExpr
    | leftOperand = applicableExpr operator = DOUBLE_PIPE rightOperand = applicableExpr
    ;

application: LPAREN (expr (COMMA expr)* COMMA?)? RPAREN;

/** Literals */
literal:
    literalBoolean
    | literalInt
    | literalFloat
    | literalChar
    | literalString
    ;

literalBoolean: TRUE | FALSE;
literalInt: LITERAL_INT;
literalFloat: LITERAL_FLOAT;
literalChar: LITERAL_CHAR;
literalString: LITERAL_STRING;

/** Identifiers */
namespaceId: ID (RSLASH ID)*;
qualifiedId: ID (DOT ID)?;
