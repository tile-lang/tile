
/* 

Tile Language Official Grammar

*/


lexer grammar tileLexer;

KW_FUNC
    : 'func'
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
KW_TRUE
    : 'true'
    ;
KW_FALSE
    : 'false'
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
    : '0' | (('-' | '+')? [1-9])+
    ;

BOOL_LITERAL
    : KW_TRUE
    | KW_FALSE
    ;

IDENTIFIER
    : [a-zA-Z_][a-zA-Z_0-9]*
    ;

WHITE_SPACE
    : [ \t\r\n]+ -> skip
    ;
