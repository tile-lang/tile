
/* 

Tile Language Official Grammar

*/


lexer grammar tileLexer;

KW_FUNC
    : 'func'
    ;
KW_REF
    : 'ref'
    ;
KW_INT
    : 'int'
    ;
KW_FLOAT
    : 'float'
    ;
KW_BOOL
    : 'bool'
    ;
KW_CHAR
    : 'char'
    ;
KW_VOID
    : 'void'
    ;
KW_WHILE
    : 'while'
    ;
KW_FOR
    : 'for'
    ;
KW_IF
    : 'if'
    ;
KW_ELSE
    : 'else'
    ;
KW_MATCH
    : 'match'
    ;
KW_RETURN
    : 'return'
    ;
KW_NATIVE
    : 'native'
    ;
KW_CINT8
    : 'ci8'
    ;
KW_CINT16
    : 'ci16'
    ;
KW_CINT32
    : 'ci32'
    ;
KW_CINT64
    : 'ci64'
    ;
KW_CUINT8
    : 'cu8'
    ;
KW_CUINT16
    : 'cu16'
    ;
KW_CUINT32
    : 'cu32'
    ;
KW_CUINT64
    : 'cu64'
    ;
KW_CFLOAT32
    : 'cf32'
    ;
KW_CFLOAT64
    : 'cf64'
    ;
KW_CPTR
    : 'cptr'
    ;
KW_CVOID
    : 'cvoid'
    ;

PUNC_LEFTPAREN
    : '('
    ;

PUNC_RIGHTPAREN
    : ')'
    ;

PUNC_LEFTBRACKET
    : '['
    ;

PUNC_RIGHTBRACKET
    : ']'
    ;

PUNC_LEFTBRACE
    : '{'
    ;

PUNC_RIGHTBRACE
    : '}'
    ;

PUNC_LESS
    : '<'
    ;

PUNC_LESSEQUAL
    : '<='
    ;

PUNC_GREATER
    : '>'
    ;

PUNC_GREATEREQUAL
    : '>='
    ;

PUNC_LEFTSHIFT
    : '<<'
    ;

PUNC_RIGHTSHIFT
    : '>>'
    ;

PUNC_PLUS
    : '+'
    ;

PUNC_PLUSPLUS
    : '++'
    ;

PUNC_MINUS
    : '-'
    ;

PUNC_MINUSMINUS
    : '--'
    ;

PUNC_STAR
    : '*'
    ;

PUNC_DIV
    : '/'
    ;

PUNC_MOD
    : '%'
    ;

PUNC_AND
    : '&'
    ;

PUNC_OR
    : '|'
    ;

PUNC_ANDAND
    : '&&'
    ;

PUNC_OROR
    : '||'
    ;

PUNC_CARET
    : '^'
    ;

PUNC_NOT
    : '!'
    ;

PUNC_TILDE
    : '~'
    ;

PUNC_QUESTION
    : '?'
    ;

PUNC_COLON
    : ':'
    ;

PUNC_SEMI
    : ';'
    ;

PUNC_COMMA
    : ','
    ;

PUNC_ASSIGN
    : '='
    ;

// '*=' | '/=' | '%=' | '+=' | '-=' | '<<=' | '>>=' | '&=' | '^=' | '|='
PUNC_STARASSIGN
    : '*='
    ;

PUNC_DIVASSIGN
    : '/='
    ;

PUNC_MODASSIGN
    : '%='
    ;

PUNC_PLUSASSIGN
    : '+='
    ;

PUNC_MINUSASSIGN
    : '-='
    ;

PUNC_LEFTSHIFTASSIGN
    : '<<='
    ;

PUNC_RIGHTSHIFTASSIGN
    : '>>='
    ;

PUNC_ANDASSIGN
    : '&='
    ;

PUNC_XORASSIGN
    : '^='
    ;

PUNC_ORASSIGN
    : '|='
    ;

PUNC_EQUAL
    : '=='
    ;

PUNC_NOTEQUAL
    : '!='
    ;

PUNC_ARROW
    : '->'
    ;

PUNC_DOT
    : '.'
    ;

PUNC_ELLIPSIS
    : '...'
    ;

INT_LITERAL
    : '0' | [1-9][0-9]* // Decimal
    | '0x' [0-9a-fA-F]+ // Hexadecimal
    | '0b' [01]+        // Binary
    ;

FLOAT_LITERAL
    : [0-9]+ '.' [0-9]* ([eE] [+-]? [0-9]+)? // 123.456, 0.456, 123.0, 123e4, 123.456e-7
    | '.' [0-9]+ ([eE] [+-]? [0-9]+)?        // .456, .456e-2
    | [0-9]+ [eE] [+-]? [0-9]+              // 123e4, 456e-2
    ;

BOOL_LITERAL
    : 'true'
    | 'false'
    ;

STRING_LITERAL
    : '"' (EscSeq | ~["\\])* '"'
    ;

// Character Literals: A single character enclosed in single quotes (')
CHAR_LITERAL
    : '\'' (EscSeq | ~['\\]) '\''
    ;

// Escape sequences for strings and characters
fragment EscSeq
    : '\\' [btnrf"'\\]  // Standard escape characters
    | '\\u' HEX HEX HEX HEX // Unicode escape
    | '\\x' HEX HEX         // Hexadecimal escape
    ;

fragment HEX
    : [0-9a-fA-F]  // A single hexadecimal digit
    ;

IDENTIFIER
    : IdentifierNondigit (IdentifierNondigit | Digit)*
    ;

fragment IdentifierNondigit
    : Nondigit
    ;

fragment Nondigit
    : [a-zA-Z_]
    ;

fragment Digit
    : [0-9]
    ;

SINGLE_LINE_COMMENT
    : '//' ~[\r\n]* -> skip
    ;

MULTI_LINE_COMMENT
    : '/*' .*? '*/' -> skip
    ;

WHITE_SPACE
    : [ \t\r\n]+ -> skip
    ;
