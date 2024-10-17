#ifndef TILE_PARSER_H_
#define TILE_PARSER_H_

#include <tile_lexer.h>
#include <tile_ast.h>

typedef struct {
    tile_token_t prev_token;
    tile_token_t current_token;
    tile_token_t next_token;
    tile_lexer_t* lexer;

}tile_parser_t;

tile_parser_t tile_parser_init(tile_lexer_t* lexer);

void tile_parser_eat(tile_parser_t* parser, token_type_t token_type);

tile_ast_t* tile_parser_parse_expression(tile_parser_t* parser);

tile_ast_t* tile_parser_parse_binary_expression(tile_parser_t* parser, tile_ast_t* left);

tile_ast_t* tile_parser_parse_statement(tile_parser_t* parser);

tile_ast_t* tile_parser_parse_statements(tile_parser_t* parser);

tile_ast_t* tile_parser_parse_for_statement(tile_parser_t* parser);

tile_ast_t* tile_parser_parse_while_statement(tile_parser_t* parser);

tile_ast_t* tile_parser_parse_if_statement(tile_parser_t* parser);

tile_ast_t* tile_parser_parse_match_statement(tile_parser_t* parser);

tile_ast_t* tile_parser_parse_option(tile_parser_t* parser);

tile_ast_t* tile_parser_parse_default_option(tile_parser_t* parser);

tile_ast_t* tile_parser_parse_variable_dec_statement(tile_parser_t* parser);

tile_ast_t* tile_parser_parse_variable_def_statement(tile_parser_t* parser);

tile_ast_t* tile_parser_parse_variable_assign(tile_parser_t* parser);

tile_ast_t* tile_parser_parse_function_statement(tile_parser_t* parser);

tile_ast_t* tile_parser_parse_return_statement(tile_parser_t* parser);

tile_ast_t* tile_parser_parse_function_argument(tile_parser_t* parser);

tile_ast_t* tile_parser_parse_block(tile_parser_t* parser);

#endif