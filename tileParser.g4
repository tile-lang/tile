/* 

Tile Language Official Grammar

*/

parser grammar tileParser;

options {
    tokenVocab = tileLexer;
}

program
    : globalStatements? EOF
    ;

globalStatements
    : globalStatement+
    ;

globalStatement
    : expressionStmt
    | variableStmt
    | funcDefStmt
    ;

localStatements
    : localStatement+
    ;

localStatement
    : expressionStmt
    | variableStmt
    | loopStmt
    | selectionStmt
    | funcDefStmt
    | returnStmt
    | blockStmt
    ;

expressionStmt
    : expression? ';'
    ;

expression
    : primaryExpression
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

primaryExpression
    : INT_LITERAL
    | FLOAT_LITERAL
    | CHAR_LITERAL
    | BOOL_LITERAL
    | IDENTIFIER
    | '(' expression ')'
    ;

unaryExpression
    : ( '++' | '--' ) IDENTIFIER
    | IDENTIFIER ( '++' | '--' )
    | unaryOperator primaryExpression
    ;

unaryOperator
    : '+'
    | '-'
    | '~'
    | '!'
    ;

funcCallExpression
    : IDENTIFIER '(' (expression)? (',' expression)* ')'
    ;

castExpression
    : '(' typeName ')' primaryExpression
    | primaryExpression
    | '(' typeName ')' unaryExpression
    | unaryExpression
    | '(' typeName ')' funcCallExpression
    | funcCallExpression
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
    | primaryExpression
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

variableStmt
    : (variableDecleration | variableDefinition | variableAssignment)
    ;

variableDecleration
    : typeName IDENTIFIER ';'
    ;

variableDefinition
    : typeName IDENTIFIER '=' expressionStmt
    ;

variableAssignment
    : IDENTIFIER assignmentOperator expressionStmt
    ;

loopStmt
    : whileStmt
    | forStmt
    ;

whileStmt
    : KW_WHILE '(' expression ')' blockStmt
    ;

forInitial
    : expressionStmt
    | variableStmt
    ;

forUpdate
    : unaryExpression
    | funcCallExpression
    | variableAssignment
    ;

forStmt
    : KW_FOR '(' forInitial expressionStmt forUpdate? ')' blockStmt
    ;

selectionStmt
    : ifStmt 
    ;

ifStmt
    : KW_IF '(' expression ')' (blockStmt) (KW_ELSE (blockStmt | ifStmt))?
    ;

funcDefStmt
    : KW_FUNC IDENTIFIER '(' (argument)? (',' argument)* ')' ':' typeName blockStmt
    ;

returnStmt
    : KW_RETURN expressionStmt
    ;

blockStmt
    : '{' localStatements? '}'
    ;

argument
    : KW_REF? typeName IDENTIFIER
    ;

typeName
    : KW_INT
    | KW_FLOAT
    | KW_BOOL
    | KW_FUNC
    | KW_VOID
    | IDENTIFIER // For custom types like structs
    ;