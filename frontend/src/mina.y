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
%epp EQ "="

%epp IDENTIFIER "<identifier>"
%epp LITERAL_BOOLEAN "<boolean literal>"
%epp LITERAL_INT "<integral literal>"
%epp LITERAL_FLOAT "<floating point literal>"
%epp LITERAL_CHAR "<character literal>"
%epp LITERAL_STRING "<string literal>"
%%
compilation_unit : module ;

module : "MODULE" qualified_id "LBRACE" declarations_opt "RBRACE" ;

qualified_id : "IDENTIFIER" | qualified_id "RSLASH" "IDENTIFIER" ;

declarations_opt : | declarations ;

declarations : declaration | declarations declaration ;

declaration : let_declaration ;

data_declaration : "DATA" "IDENTIFIER" "LBRACE" "RBRACE" ;

let_declaration : "LET" "IDENTIFIER" "EQ" expr ;

expr : if | literal | reference | paren_expr ;

/* expr : inner_expr applications_opt ;

inner_expr : if | literal | reference | paren_expr ; */

/* applications_opt : | applications ;

applications : application | applications application ;

application : "LPAREN" arguments_opt "RPAREN" ; */

arguments_opt : | arguments ;

arguments: expr | arguments "COMMA" expr ;

if : "IF" expr "THEN" expr "ELSE" expr ;

literal : "LITERAL_BOOLEAN" | "LITERAL_INT" | "LITERAL_FLOAT" | "LITERAL_CHAR" | "LITERAL_STRING" ;

reference : "IDENTIFIER" ;

paren_expr : "LPAREN" expr "RPAREN" ;