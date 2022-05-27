%start compilation_unit

%epp MODULE "module"
%epp DATA "data"
%epp LET "let"
%epp IF "if"
%epp THEN "then"
%epp ELSE "else"

%epp LBRACE "{"
%epp RBRACE "}"
%epp LPAREN "("
%epp RPAREN ")"
%epp RSLASH "/"
%epp COMMA ","
%epp EQ "="

%epp IDENTIFIER "<identifier>"
%epp LITERAL_BOOLEAN "<boolean literal>"
%epp LITERAL_INT "<integral literal>"
%epp LITERAL_FLOAT "<floating point literal>"
%epp LITERAL_CHAR "<character literal>"
%epp LITERAL_STRING "<string literal>"

%%

/* File */
compilation_unit : module | compilation_unit module ;

/* Module */
module : "MODULE" module_id "LBRACE" imports declarations "RBRACE" ;

/* Imports */
imports : | imports import;

import
	: "IMPORT" module_id
	| "IMPORT" module_id "DOT" "IDENTIFIER"
	| "IMPORT" module_id "DOT" "LBRACE" symbols "RBRACE";

symbols : "IDENTIFIER" | symbols "COMMA" "IDENTIFIER" ;

/* Declarations */
declarations : | declarations declaration ;

declaration : let_declaration | data_declaration ;

data_declaration : "DATA" "IDENTIFIER" "LBRACE" "RBRACE" ;

let_declaration : "LET" "IDENTIFIER" "EQ" expr ;

/* Expressions */
expr : expr_not_ref | reference ;
expr_not_ref : if | lambda | literal | applicable_expr_not_ref ;

if : "IF" expr "THEN" expr "ELSE" expr ;

lambda : "IDENTIFIER" "ARROW" expr | lambda_params "ARROW" expr ;

ids : "IDENTIFIER" "COMMA" "IDENTIFIER" | ids "COMMA" "IDENTIFIER" ;

lambda_params : "LPAREN" "RPAREN" | "LPAREN" "IDENTIFIER" "RPAREN" | "LPAREN" ids "RPAREN" ;

literal : "LITERAL_BOOLEAN" | "LITERAL_INT" | "LITERAL_FLOAT" | "LITERAL_CHAR" | "LITERAL_STRING" ;

applicable_expr : applicable_expr_not_ref | reference ;
applicable_expr_not_ref
	: "LPAREN" expr_not_ref "RPAREN"
	| "LPAREN" reference "RPAREN"
	| "LPAREN" "IDENTIFIER" "RPAREN"
	| applicable_expr application ;

reference : reference_not_id | "IDENTIFIER" ;
reference_not_id : module_id "DOT" "IDENTIFIER" ;

module_id : module_id_not_id | "IDENTIFIER" ;
module_id_not_id : module_id "RSLASH" "IDENTIFIER" ;

application : "LPAREN" arguments "RPAREN" ;

arguments: | expr | arguments "COMMA" expr ;
