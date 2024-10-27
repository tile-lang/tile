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

statement : '.' ; // TODO: create these statements
    // : expression_stmt
    // | func_def_stmt
    // | block_stmt
    // | selection_stmt
    // | loop_stmt
    // | return_stmt
    // ;

