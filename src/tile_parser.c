#include <tile_parser.h>
#include <assert.h>
#include <stdbool.h>

#define STB_DS_IMPLEMENTATION
#include <stb_ds.h>

static primitive_type tile_parser_parse_primitive_type(tile_parser_t* parser) {
    if (strcmp("int", parser->current_token.value) == 0)
        return PRIM_TYPE_INT;
    if (strcmp("float", parser->current_token.value) == 0)
        return PRIM_TYPE_FLOAT;
    return PRIM_TYPE_NONE;
}

tile_parser_t tile_parser_init(tile_lexer_t* lexer) {
    tile_parser_t parser = {
        .lexer = lexer,
        .prev_token = tile_token_create(TOKEN_NONE, NULL),
        .current_token = tile_token_create(TOKEN_NONE, NULL),
        .next_token = tile_lexer_get_next_token(lexer),
    };
    return parser;
}

void tile_parser_eat(tile_parser_t* parser, token_type_t token_type) {
    if (parser->current_token.type != token_type) {
        // TODO: print row and col of unexpected token
        printf(
            "%s:%d:%d:Unexpected token %s, with type %d, Expected was %d\n",
            parser->lexer->loc.file_name,
            parser->lexer->loc.row,
            parser->lexer->loc.col,
            parser->current_token.value,
            parser->current_token.type,
            token_type
        );
    } else {
        parser->prev_token = parser->current_token;
        parser->current_token = parser->next_token;
        parser->next_token = tile_lexer_get_next_token(parser->lexer);
    }
}

tile_ast_t* tile_parser_parse_expression(tile_parser_t* parser) {
    // "test"
    // 3
    tile_ast_t* left;
    switch (parser->current_token.type) {
    
    case TOKEN_INT_LITERAL:
        left = tile_ast_create((tile_ast_t) {
            .expression.number.text_value = parser->current_token.value,
            .expression.number.ivalue = atoi(parser->current_token.value),
            .expression.expression_kind = EXPR_LIT_INT,
            .tag = AST_EXPRESSION,
        });
        tile_parser_eat(parser, TOKEN_INT_LITERAL);
        break;
    
    case TOKEN_FLOAT_LITERAL:
        left = tile_ast_create((tile_ast_t) {
            .expression.number.text_value = parser->current_token.value,
            .expression.number.fvalue = atof(parser->current_token.value),
            .expression.expression_kind = EXPR_LIT_FLOAT,
            .tag = AST_EXPRESSION,
        });
        tile_parser_eat(parser, TOKEN_FLOAT_LITERAL);
        break;
    
    case TOKEN_STRING_LITERAL:
        left = tile_ast_create((tile_ast_t) {
            .expression.string.text_value = parser->current_token.value,
            .expression.string.string_value = parser->current_token.value,
            .expression.string.length = strlen(parser->current_token.value),
            .expression.expression_kind = EXPR_LIT_STRING,
            .tag = AST_EXPRESSION,
        });
        tile_parser_eat(parser, TOKEN_STRING_LITERAL);
        break;

    case TOKEN_ID:
        left = tile_ast_create((tile_ast_t) {
            .expression.variable.name = parser->current_token.value,
            .expression.expression_kind = EXPR_VARIABLE,
            .tag = AST_EXPRESSION,
        });
        tile_parser_eat(parser, TOKEN_ID);
        break;

    default:
    break;
    }
    // assert(false && "TODO: Expression parsing is not totally implemented yet.");
    return tile_parser_parse_binary_expression(parser, left);
}

tile_ast_t* tile_parser_parse_binary_expression(tile_parser_t* parser, tile_ast_t* left) {
    while (parser->current_token.type == TOKEN_PLUS  ||
           parser->current_token.type == TOKEN_MINUS ||
           parser->current_token.type == TOKEN_STAR  ||
           parser->current_token.type == TOKEN_SLASH) {
        binary_operator_t op;
        switch (parser->current_token.type) {
            case TOKEN_PLUS:
                op = OP_ADD;
            break;
        
            case TOKEN_MINUS:
                op = OP_SUB;
            break;

            case TOKEN_STAR:
                op = OP_MULT;
            break;
        
            case TOKEN_SLASH:
                op = OP_DIV;
            break;
        
            default:
                // TODO: write a valid default case
            break;
        }
        tile_parser_eat(parser, parser->current_token.type);

        tile_ast_t* right = tile_parser_parse_expression(parser);

        left = tile_ast_create((tile_ast_t) {
            .expression.binary_expr.op = op,
            .expression.binary_expr.left = left,
            .expression.binary_expr.right = right,
            .expression.expression_kind = EXPR_BINARY,
            .tag = AST_EXPRESSION,
        });
    }
    return left;
}

tile_ast_t* tile_parser_parse_statement(tile_parser_t* parser) {
    switch (parser->current_token.type) {
    
    case TOKEN_WHILE:
        return tile_parser_parse_while_statement(parser);
        break;

    case TOKEN_FOR:
        return tile_parser_parse_for_statement(parser);
        break;

    case TOKEN_IF:
        return tile_parser_parse_if_statement(parser);
        break;

    case TOKEN_MATCH:
        return tile_parser_parse_match_statement(parser);
        break;

    case TOKEN_TYPE_KW:
        if (strcmp(parser->current_token.value, "func") == 0)
            return tile_parser_parse_function_statement(parser);
        else
            return tile_parser_parse_variable_dec_statement(parser);
        break;
    
    case TOKEN_ID:
        return tile_parser_parse_variable_def_statement(parser);
        break;

    case TOKEN_RETURN:
        return tile_parser_parse_return_statement(parser);
        break;
    
    case TOKEN_COMMENT:
        tile_parser_eat(parser, TOKEN_COMMENT);
        break;

    case TOKEN_UNKNOWN:
    default:
        //TODO: implement an err function or something
        tile_parser_eat(parser, 1000);
        tile_parser_eat(parser, TOKEN_UNKNOWN);
        break;
    }
    return NULL;
}

tile_ast_t* tile_parser_parse_statements(tile_parser_t* parser) {
    tile_ast_t** statements = NULL;
    while (parser->current_token.type != TOKEN_EOF) {
        tile_ast_t* statement;
        statement = tile_parser_parse_statement(parser);
        arrput(statements, statement);
    }
    tile_ast_t* program = tile_ast_create((tile_ast_t) {
        .program.statements = statements,
        .program.statement_count = arrlen(statements),
        .tag = AST_PROGRAM,
    });

    return program;
}

tile_ast_t* tile_parser_parse_for_statement(tile_parser_t* parser) {
    // for (initialization; condition; update) 
    // { } // block part 

    tile_parser_eat(parser, TOKEN_FOR);
    tile_parser_eat(parser, TOKEN_LPAREN);
    tile_ast_t* initialization = tile_parser_parse_statement(parser); // initialization part
    printf("Parsed initialization\n"); // Debugging output
    tile_parser_eat(parser, TOKEN_SEMI);
    tile_ast_t* condition = tile_parser_parse_expression(parser); // condition part
    printf("Parsed condition\n"); // Debugging output
    tile_parser_eat(parser, TOKEN_SEMI);
    tile_ast_t* update = tile_parser_parse_expression(parser); // update part
    printf("Parsed update\n"); // Debugging output
    tile_parser_eat(parser, TOKEN_RPAREN);
    tile_ast_t* body = tile_parser_parse_block(parser); // body part
    printf("Parsed body\n"); // Debugging output
    tile_ast_t* for_statement = tile_ast_create((tile_ast_t) {
        .for_statement.initialization = initialization,
        .for_statement.condition = condition,
        .for_statement.update = update,
        .for_statement.body = body,
        .tag = AST_FOR_STATEMENT,
    });
    return for_statement;
}

tile_ast_t* tile_parser_parse_while_statement(tile_parser_t* parser) {
    // while (expression) 
    // { } // block part 

    tile_parser_eat(parser, TOKEN_WHILE);
    tile_parser_eat(parser, TOKEN_LPAREN);
    tile_ast_t* condition = tile_parser_parse_expression(parser); // condition part
    tile_parser_eat(parser, TOKEN_RPAREN);
    tile_ast_t* body = tile_parser_parse_block(parser); // body part
    tile_ast_t* while_statement = tile_ast_create((tile_ast_t) {
        .while_statement.condition = condition,
        .while_statement.body = body,
        .tag = AST_WHILE_STATEMENT,
    });
    return while_statement;
}

tile_ast_t* tile_parser_parse_if_statement(tile_parser_t* parser) {
    // if (condition) 
    // { } // block part 

    tile_parser_eat(parser, TOKEN_IF);
    tile_parser_eat(parser, TOKEN_LPAREN);
    tile_ast_t* condition = tile_parser_parse_expression(parser); // condition part
    tile_parser_eat(parser, TOKEN_RPAREN);
    tile_ast_t* body = tile_parser_parse_block(parser);
    
    tile_ast_t* altarnate = NULL;
    if (parser->current_token.type == TOKEN_ELSE) { // 'else'
        tile_parser_eat(parser, TOKEN_ELSE);
        if (parser->current_token.type == TOKEN_IF) {  // 'else if'
            altarnate = tile_parser_parse_if_statement(parser);
        }
        else { // There is no 'if', only 'else'
            altarnate = tile_parser_parse_block(parser);
        }
    }

    tile_ast_t* if_statement = tile_ast_create((tile_ast_t){
        .if_statement.condition = condition,
        .if_statement.body = body,
        .if_statement.altarnate = altarnate,
        .tag = AST_IF_STATEMENT,
    });
    
    return if_statement;
}

tile_ast_t* tile_parser_parse_match_statement(tile_parser_t* parser) {
    // match (expression) {
    //     option condition:
    //         statements
    //     option condition:
    //         statements
    //     default:
    //         statements
    // }
    tile_parser_eat(parser, TOKEN_MATCH);
    tile_parser_eat(parser, TOKEN_LPAREN);
    tile_ast_t* expression = tile_parser_parse_expression(parser);
    tile_parser_eat(parser, TOKEN_RPAREN);
    size_t default_number = 0;

    tile_parser_eat(parser, TOKEN_LBRACE);
    tile_ast_t** options = NULL;
    tile_ast_t* default_option = NULL;
    while (parser->current_token.type != TOKEN_RBRACE) {
        if (parser->current_token.type == TOKEN_OPTION) {
            tile_ast_t* option = tile_parser_parse_option(parser);
            arrput(options, option);
        }
        else if (parser->current_token.type == TOKEN_DEFAULT) {
            default_option = tile_parser_parse_default_option(parser);
            default_number++;
        }
    }
    tile_parser_eat(parser, TOKEN_RBRACE);
    
    tile_ast_t* match_statement = tile_ast_create((tile_ast_t) {
        .match_statement.expression = expression,
        .match_statement.options = options,
        .match_statement.default_option = default_option,
        .match_statement.option_count = arrlen(options) + default_number,
        .tag = AST_MATCH_STATEMENT,
    });
    
    return match_statement;
}

tile_ast_t* tile_parser_parse_option(tile_parser_t* parser) {
    // option condition:
    //     statements
    tile_parser_eat(parser, TOKEN_OPTION);
    tile_ast_t* condition = tile_parser_parse_expression(parser);
    tile_parser_eat(parser, TOKEN_COLON);
    tile_ast_t** statements = NULL;

    while (parser->current_token.type != TOKEN_OPTION 
          && parser->current_token.type != TOKEN_DEFAULT 
          && parser->current_token.type != TOKEN_RBRACE) {
        tile_ast_t* statement;
        statement = tile_parser_parse_statement(parser);
        arrput(statements, statement);
    }

    tile_ast_t* option = tile_ast_create((tile_ast_t) {
        .option_statement.condition = condition,
        .option_statement.statements = statements,
        .option_statement.statement_count = arrlen(statements),
        .tag = AST_OPTION_STATEMENT,
    });

    return option;
}

tile_ast_t* tile_parser_parse_default_option(tile_parser_t* parser) {
    // default:
    //     statements
    tile_parser_eat(parser, TOKEN_DEFAULT);
    tile_parser_eat(parser, TOKEN_COLON);
    tile_ast_t** statements = NULL;
    while (parser->current_token.type != TOKEN_RBRACE) {
        tile_ast_t* statement;
        statement = tile_parser_parse_statement(parser);
        arrput(statements, statement);
    }
    tile_ast_t* default_option = tile_ast_create((tile_ast_t) {
        .default_statement.statements = statements,
        .default_statement.statement_count = arrlen(statements),
        .tag = AST_DEFAULT_STATEMENT,
    });
    return default_option;
}

tile_ast_t* tile_parser_parse_variable_dec_statement(tile_parser_t* parser) {
    // int x;
    // float y;
    // int x = 4;
    primitive_type type_name = tile_parser_parse_primitive_type(parser);
    tile_parser_eat(parser, TOKEN_TYPE_KW);

    const char* var_name = parser->current_token.value;
    tile_parser_eat(parser, TOKEN_ID);
    tile_ast_t* expression = NULL;
    if (parser->current_token.type == TOKEN_ASSIGN) {
        // this is a decleration with initial value
        tile_parser_eat(parser, TOKEN_ASSIGN);
        expression = tile_parser_parse_expression(parser);
    }
    tile_parser_eat(parser, TOKEN_SEMI);
    tile_ast_t* var_decl_statement = tile_ast_create((tile_ast_t) {
        .variable_decl.type = type_name,
        .variable_decl.name = var_name,
        .variable_decl.value = expression,
        .tag = AST_VARIABLE_DECL,
    });

    return var_decl_statement;
}

tile_ast_t* tile_parser_parse_variable_def_statement(tile_parser_t* parser) {
    tile_parser_eat(parser, TOKEN_ID);
    return tile_parser_parse_variable_assign(parser);
}

tile_ast_t* tile_parser_parse_variable_assign(tile_parser_t* parser) {
    // z = 3;
    const char* var_name = parser->prev_token.value;
    tile_parser_eat(parser, TOKEN_ASSIGN);
    tile_ast_t* expression = tile_parser_parse_expression(parser); // stored value
    tile_parser_eat(parser, TOKEN_SEMI);

    tile_ast_t* var_assign_statement = tile_ast_create((tile_ast_t) {
        .variable_assign.name = var_name,
        .variable_assign.value = expression,
        .tag = AST_VARIABLE_ASSIGN,
    });

    return var_assign_statement;
}

tile_ast_t* tile_parser_parse_function_statement(tile_parser_t* parser) {
    // func name(args): int {
    //  statements
    //  return expression
    // }

    tile_parser_eat(parser, TOKEN_TYPE_KW);
    const char* func_name = parser->current_token.value;
    tile_parser_eat(parser, TOKEN_ID);
    tile_parser_eat(parser, TOKEN_LPAREN);
    tile_ast_t** args = NULL;

    if (parser->current_token.type != TOKEN_RPAREN) {
        tile_ast_t* arg = tile_parser_parse_function_argument(parser);
        arrput(args, arg);
    }
    while (parser->current_token.type != TOKEN_RPAREN) {
        tile_ast_t* arg;
        tile_parser_eat(parser, TOKEN_COMMA);
        arg = tile_parser_parse_function_argument(parser);
        arrput(args, arg);
    }

    tile_parser_eat(parser, TOKEN_RPAREN);
    tile_parser_eat(parser, TOKEN_COLON);

    primitive_type return_type_name = tile_parser_parse_primitive_type(parser);
    tile_ast_t* return_type = tile_ast_create((tile_ast_t) {
        .return_type.type_name = return_type_name,
        .tag = AST_FUNCTION_RETURN_TYPE,
    });

    tile_parser_eat(parser, TOKEN_TYPE_KW);

    tile_ast_t* block = tile_parser_parse_block(parser);

    tile_ast_t* function = tile_ast_create((tile_ast_t) {
        .function_statement.return_type = return_type,
        .function_statement.func_name = func_name,
        .function_statement.arguments = args,
        .function_statement.argument_count = arrlen(args),
        .function_statement.body = block,
        .tag = AST_FUNCTION_STATEMENT,
    });

    return function;
}

tile_ast_t* tile_parser_parse_function_argument(tile_parser_t* parser) {
    // func name(args): int
    primitive_type type_name = tile_parser_parse_primitive_type(parser);
    tile_parser_eat(parser, TOKEN_TYPE_KW);
    const char* var_name = parser->current_token.value;
    tile_parser_eat(parser, TOKEN_ID);

    tile_ast_t* argument = tile_ast_create((tile_ast_t) {
        .argument.type_name = type_name,
        .argument.var_name = var_name,
        .tag = AST_FUNCTION_ARGUMENT,
    });

    return argument;
}

tile_ast_t* tile_parser_parse_return_statement(tile_parser_t* parser) {
    // return expression;
    tile_parser_eat(parser, TOKEN_RETURN);
    tile_ast_t* expression = tile_parser_parse_expression(parser);
    tile_parser_eat(parser, TOKEN_SEMI);

    tile_ast_t* return_statement = tile_ast_create((tile_ast_t) {
        .return_statement.expression = expression,
        .tag = AST_RETURN_STATEMENT,
    });

    return return_statement;
}

tile_ast_t* tile_parser_parse_block(tile_parser_t* parser) {
    // {
    //     statements
    // }

    tile_parser_eat(parser, TOKEN_LBRACE);
    tile_ast_t** statements = NULL;
    
    while (parser->current_token.type != TOKEN_RBRACE) {
        tile_ast_t* statement;
        statement = tile_parser_parse_statement(parser);
        arrput(statements, statement);
    }

    tile_parser_eat(parser, TOKEN_RBRACE);

    tile_ast_t* block = tile_ast_create((tile_ast_t) {
        .block.statements = statements,
        .block.statement_count = arrlen(statements),
        .tag = AST_BLOCK,
    });
    return block;
}