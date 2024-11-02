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
    : loopStmt
    | blockStmt
    | selectionStmt
    | expressionStmt
    | funcDefStmt
    ; 
    // TODO: create these statements
    // : expression_stmt
    // | return_stmt
    // | loop_stmt
    // | selection_stmt
    // | block_stmt
    // ;

expressionStmt
    : expression? ';'
    ;

expression
    : primaryExpr
    | unaryExpression
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
    | funcCallExpr
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

blockStmt
    : '{' statements? '}'
    ;

funcDefStmt
    : KW_FUNC IDENTIFIER '(' parameters? ')' blockStmt
    ;

funcCallExpr
    : IDENTIFIER '(' arguments? ')'
    ;

parameters
    : IDENTIFIER (',' IDENTIFIER)*
    ;

arguments
    : expression (',' expression)*
    ;

typeName
    : KW_INT
    | KW_FLOAT
    | KW_BOOL
    | KW_FUNC
    ;