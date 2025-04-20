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
    : variableStmt
    | funcDefStmt
    | nativeFuncDeclStmt
    | typeDefinition
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
    | tasmBlock
    | blockStmt
    ;

expressionStmt
    : expression? ';'
    ;

expression
    : primaryExpression
    | unaryExpression
    | arrayIndexAccessor
    | arrayValuedInitializer
    | arraySizedInitializer
    | objectLiteralExpression
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
    | STRING_LITERAL
    | IDENTIFIER
    | '(' expression ')'
    ;

objectLiteralExpression
    :
    PUNC_LEFTBRACE ( objectLiteralFieldAssignment (PUNC_COMMA objectLiteralFieldAssignment)*)? PUNC_COMMA? PUNC_RIGHTBRACE
    ;

objectLiteralFieldAssignment
    : '.' IDENTIFIER assignmentOperator expression
    ;

unaryExpression
    : incDecOperator primaryExpression
    | primaryExpression incDecOperator
    | unaryOperator primaryExpression
    | unaryOperator funcCallExpression
    | unaryOperator castExpression
    ;

incDecOperator
    : ( '++' | '--' )
    ;

unaryOperator
    : '+'
    | '-'
    | '~'
    | '!'
    ;

arrayValuedInitializer
    : '[' arrayValuedInitializerElements? ']'
    ;

arrayValuedInitializerElements
    : expression (PUNC_COMMA expression)*
    | arrayValuedInitializer (PUNC_COMMA arrayValuedInitializer)*
    ;

arraySizedInitializer
    : primaryTypeName arraySizeSpecifier+
    ;

arraySizeSpecifier
    : '[' expression ']'
    ;

arrayIndexAccessor
    : IDENTIFIER arrayIndexSpecifier+
    ;

arrayIndexSpecifier
    : '[' expression ']'
    ;

arrayIndexAccessorSetter
    : IDENTIFIER arrayIndexSpecifier+
    ;

funcCallExpression
    : IDENTIFIER '(' (expression)? (PUNC_COMMA expression)* ')'
    | primaryExpression '.' IDENTIFIER '(' (expression)? (PUNC_COMMA expression)* ')'
    | funcCallExpression '.' IDENTIFIER '(' (expression)? (PUNC_COMMA expression)* ')'
    ;

castExpression
    : '(' typeName ')' primaryExpression
    | primaryExpression
    | '(' typeName ')' unaryExpression
    | unaryExpression
    | '(' typeName ')' funcCallExpression
    | funcCallExpression
    | '(' typeName ')' arrayIndexAccessor
    | arrayIndexAccessor
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

andExpression
    : shiftExpression ('&' shiftExpression)*
    ;

exclusiveOrExpression
    : andExpression ('^' andExpression)*
    ;

inclusiveOrExpression
    : exclusiveOrExpression ('|' exclusiveOrExpression)*
    ;

relationalExpression
    : inclusiveOrExpression (('<' | '>' | '<=' | '>=') inclusiveOrExpression)*
    ;

equalityExpression
    : relationalExpression (('==' | '!=') relationalExpression)*
    ;

logicalAndExpression
    : equalityExpression ('&&' equalityExpression)*
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
    : IDENTIFIER ':' typeName ';'
    | typeName IDENTIFIER ';'
    ;

variableDefinition
    : IDENTIFIER ':' typeName '=' expressionStmt
    | typeName IDENTIFIER '=' expressionStmt
    ;

variableAssignment
    : IDENTIFIER assignmentOperator expressionStmt
    | arrayIndexAccessorSetter assignmentOperator expressionStmt
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
    | forUpdateAssingment
    ;

forUpdateAssingment
    : IDENTIFIER assignmentOperator expression
    | arrayIndexAccessorSetter assignmentOperator expression
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
    : KW_FUNC IDENTIFIER '(' (argument)? (PUNC_COMMA argument)* ')' ':' typeName blockStmt
    ;

nativeFuncDeclStmt
    : KW_NATIVE KW_FUNC IDENTIFIER '(' (cArgument)? (PUNC_COMMA cArgument)* ')' ':' cTypeName ';'
    ;

returnStmt
    : KW_RETURN expressionStmt
    ;

tasmBlock
    : KW_TASM PUNC_LEFTBRACE tasmInstructions PUNC_RIGHTBRACE
    ;

tasmInstructions
    : (tasmLine)? (PUNC_COMMA tasmLine)*
    ;

tasmLine
    : STRING_LITERAL
    ;

blockStmt
    : PUNC_LEFTBRACE localStatements? PUNC_RIGHTBRACE
    ;

argument
    : IDENTIFIER ':' KW_REF? typeName
    // : KW_REF? typeName IDENTIFIER
    ;

cArgument
    : (IDENTIFIER ':')? cTypeName
    // : cTypeName IDENTIFIER?
    ;

typeName
    : primaryTypeName
    | primaryArrTypeName
    ;

primaryArrTypeName
    : primaryTypeName ('['']')+
    ;

primaryTypeName
    : KW_INT
    | KW_FLOAT
    | KW_BOOL
    | KW_CHAR
    | KW_FUNC
    | KW_VOID
    | KW_STRING
    | IDENTIFIER // For custom types like structs
    ;

cTypeName
    : KW_CINT8
    | KW_CINT16
    | KW_CINT32
    | KW_CINT64
    | KW_CUINT8
    | KW_CUINT16
    | KW_CUINT32
    | KW_CUINT64
    | KW_CFLOAT32
    | KW_CFLOAT64
    | KW_CPTR
    | KW_CVOID
    // | IDENTIFIER // For custom types like structs
    ;

typeDefinition
    : KW_TYPE IDENTIFIER structDefinition
    | KW_TYPE IDENTIFIER ':' typeUnion ';'
    ;

structDefinition
    : PUNC_LEFTBRACE fieldDefinition* PUNC_RIGHTBRACE
    ;

fieldDefinition
    : IDENTIFIER ':' typeName ';'
    | typeName IDENTIFIER ';'
    ;

typeUnion
    : IDENTIFIER ('|' IDENTIFIER)*
    ;
