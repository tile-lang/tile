/* 

Tile Language Official Grammar

*/

parser grammar tileParser;

options {
    tokenVocab = tileLexer;
}

program
    : statements? EOF
    ;

statements
    : statement+
    ;

statement
    : expressionStmt
    | loopStmt
    | selectionStmt
    | funcDefStmt
    | blockStmt
    ; 
    // TODO: create these statements
    // | func_def_stmt
    // | return_stmt
    // ;

expressionStmt
    : expression? ';'
    ;

expression
    : primaryExpr
    | unaryExpression
    | funcCallExpression
    | castExpression
    | multiplicativeExpression
    | additiveExpression
    | shiftExpression
    | relationalExpression
    | equalityExpression
    | andExpression
    | exclusiveOrExpression
    | inclusiveOrExpression
    | logicalAndExpression
    | logicalOrExpression
    | conditionalExpression
    | assignmentExpression
    ;

primaryExpr
    : INT_LITERAL
    | BOOL_LITERAL
    | IDENTIFIER
    | '(' expression ')'
    ;

unaryExpression
    : ( '++' | '--' ) IDENTIFIER
    | IDENTIFIER ( '++' | '--' )
    | unaryOperator primaryExpr
    ;

unaryOperator
    // : '+'
    // | '-'
    : '~'
    | '!'
    ;

funcCallExpression
    : IDENTIFIER '(' (expression)? (',' expression)* ')'
    ;

castExpression
    : '(' typeName ')' primaryExpr
    | unaryExpression
    | primaryExpr
    ;

multiplicativeExpression
    : castExpression (( '*' | '/' | '%' ) castExpression)*
    ;

additiveExpression
    : multiplicativeExpression (( '+' | '-' ) multiplicativeExpression)*
    ;

shiftExpression
    : additiveExpression (('<<' | '>>') additiveExpression)*
    ;

relationalExpression
    : shiftExpression (('<' | '>' | '<=' | '>=') shiftExpression)*
    ;

equalityExpression
    : relationalExpression (('==' | '!=') relationalExpression)*
    ;

andExpression
    : equalityExpression ('&' equalityExpression)*
    ;

exclusiveOrExpression
    : andExpression ('^' andExpression)*
    ;

inclusiveOrExpression
    : exclusiveOrExpression ('|' exclusiveOrExpression)*
    ;

logicalAndExpression
    : inclusiveOrExpression ('&&' inclusiveOrExpression)*
    ;

logicalOrExpression
    : logicalAndExpression ('||' logicalAndExpression)*
    ;

conditionalExpression
    : logicalOrExpression ('?' expression ':' conditionalExpression)?
    ;

assignmentExpression
    : conditionalExpression
    | unaryExpression assignmentOperator assignmentExpression
    | primaryExpr
    ;

assignmentOperator
    : '='
    | '*='
    | '/='
    | '%='
    | '+='
    | '-='
    | '<<='
    | '>>='
    | '&='
    | '^='
    | '|='
    ;

loopStmt
    : whileStmt
    ;

whileStmt
    : KW_WHILE '(' expression ')' blockStmt
    ;

// for_stmt
//     : KW_FOR '(' ')'
//     ;

selectionStmt
    : ifStmt 
    ;

ifStmt
    : KW_IF '(' expression ')' blockStmt (KW_ELSE (blockStmt | ifStmt))?
    ;

funcDefStmt
    : KW_FUNC IDENTIFIER '(' (argument)? (',' argument)* ')' ':' typeName blockStmt
    ;

blockStmt
    : '{' statements? '}'
    ;

argument
    : typeName IDENTIFIER
    ;

typeName
    : KW_INT
    | KW_FLOAT
    | KW_BOOL
    | KW_FUNC
    ;