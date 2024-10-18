#include <tile_ast.h>

#define ARENA_IMPLEMENTATION

#include "common/arena.h"
#include <stb_ds.h>

#define ARENA_SIZE 1024

arena_t* ast_arena;

void tile_ast_arena_init() {
    ast_arena = arena_init(ARENA_SIZE);
}

void tile_ast_arena_destroy() {
    arena_destroy(ast_arena);
}

tile_ast_t* tile_ast_create(tile_ast_t ast) {
    tile_ast_t* ptr = arena_alloc(&ast_arena, sizeof(tile_ast_t));
    if (ptr)
        *ptr = ast;
    return ptr;
}

void tile_ast_destroy(tile_ast_t* node) {

    switch (node->tag)
    {
    case AST_PROGRAM:
        for(size_t i = 0; i < node->program.statement_count; i++) {
            tile_ast_destroy(node->program.statements[i]);
        }
        arrfree(node->program.statements);
        break;
    
    case AST_MATCH_STATEMENT:
        if (node->match_statement.default_option != NULL) {
            // Match statement has a default option
            for(size_t i = 0; i < node->match_statement.option_count - 1; i++) {
                tile_ast_destroy(node->match_statement.options[i]);
            }
            tile_ast_destroy(node->match_statement.default_option);
        }
        else {
            // Match statement hasn't a default option
            for(size_t i = 0; i < node->match_statement.option_count; i++) {
                tile_ast_destroy(node->match_statement.options[i]);
            } 
        }
        arrfree(node->match_statement.options);
        break;

    case AST_OPTION_STATEMENT:
        for(size_t i = 0; i < node->option_statement.statement_count; i++) {
            tile_ast_destroy(node->option_statement.statements[i]);
        }
        arrfree(node->option_statement.statements);
        break;

    case AST_DEFAULT_STATEMENT:
        for(size_t i = 0; i < node->default_statement.statement_count; i++) {
            tile_ast_destroy(node->default_statement.statements[i]);
        }
        arrfree(node->default_statement.statements);
        break;        

    case AST_WHILE_STATEMENT:
        tile_ast_destroy(node->while_statement.body);
        break;
    
    case AST_IF_STATEMENT:
        tile_ast_destroy(node->if_statement.body);
        break;
        
    case AST_BLOCK:
        for(size_t i = 0; i < node->block.statement_count; i++) {
            tile_ast_destroy(node->block.statements[i]);
        }
        arrfree(node->block.statements);
        break;

    case AST_FUNCTION_STATEMENT:
        arrfree(node->function_statement.arguments);
        tile_ast_destroy(node->function_statement.body);
        break;

    default:
        break;
    }
}

static void print_indent(int indent) {
    for (int i = 0; i < indent; i++) {
        printf("  ");
    }
}

void tile_ast_show(tile_ast_t* node, int indent) {
    if (!node) return;

    print_indent(indent);
    switch (node->tag) {
        case AST_NONE:
            printf("NONE\n");
            break;

        case AST_PROGRAM:
            printf("PROGRAM\n");
            for (size_t i = 0; i < node->program.statement_count; i++) {
                tile_ast_show(node->program.statements[i], indent + 1);
            }
            break;

        // EXPRESSIONS
        case AST_EXPRESSION:
            switch (node->expression.expression_kind) {
                case EXPR_VARIABLE:
                    printf("EXPR_VARIABLE\n");
                break;
                
                case EXPR_LIT_INT:
                    printf("EXPR_LIT_INT\n");
                break;
                
                case EXPR_LIT_FLOAT:
                    printf("EXPR_LIT_FLOAT\n");
                break;
                
                case EXPR_LIT_STRING:
                    printf("EXPR_LIT_STRING\n");
                break;
                
                case EXPR_BINARY:
                    printf("EXPR_BINARY\n");
                    tile_ast_show(node->expression.binary_expr.left, indent + 2);
                    print_indent(indent + 2);
                    printf("%d\n", node->expression.binary_expr.op);
                    tile_ast_show(node->expression.binary_expr.right, indent + 2);
                break;
                
                case EXPR_UNARY:
                    printf("EXPR_UNARY\n");
                break;

                default:
                    printf("\n");
                break;
            }
            break;

        case AST_VARIABLE_DECL:
            printf("VARIABLE_DECL\n");
            tile_ast_show(node->variable_decl.value, indent + 2);
            break;

        case AST_VARIABLE_ASSIGN:
            printf("VARIABLE_ASSIGN\n");
            tile_ast_show(node->variable_assign.value, indent + 2);
            break;

        case AST_WHILE_STATEMENT:
            printf("WhileStmt:\n");
            print_indent(indent + 1);
            printf("Condition:\n");
            tile_ast_show(node->while_statement.condition, indent + 2);
            print_indent(indent + 1);
            printf("Body:\n");
            tile_ast_show(node->while_statement.body, indent + 2);
            break;

        case AST_FOR_STATEMENT:
            printf("ForStmt:\n");
            print_indent(indent + 1);
            printf("Initialization:\n");
            tile_ast_show(node->for_statement.initialization, indent + 2);
            print_indent(indent + 1);
            printf("Condition:\n");
            tile_ast_show(node->for_statement.condition, indent + 2);
            print_indent(indent + 1);
            printf("Update:\n");
            tile_ast_show(node->for_statement.update, indent + 2);
            print_indent(indent + 1);
            printf("Body:\n");
            tile_ast_show(node->for_statement.body, indent + 2);
            break;
    
        case AST_IF_STATEMENT:
            printf("IfStmt\n");
            print_indent(indent + 1);
            printf("Condition:\n");
            tile_ast_show(node->if_statement.condition, indent + 2);
            print_indent(indent + 1);
            printf("Body:\n");
            tile_ast_show(node->if_statement.body, indent + 2);
            print_indent(indent + 1);
            printf("Altarnate:\n");
            tile_ast_show(node->if_statement.altarnate, indent + 2);
            break;

        case AST_MATCH_STATEMENT:
            printf("MatchStmt\n");
            print_indent(indent + 1);
            printf("Expression:\n");
            tile_ast_show(node->match_statement.expression, indent + 2);
            print_indent(indent + 1);
            printf("Options:\n");
            
            if (node->match_statement.default_option != NULL) {
                // Match statement has a default option
                for(size_t i = 0; i < node->match_statement.option_count - 1; i++) {
                    tile_ast_show(node->match_statement.options[i], indent + 2);
                }
                print_indent(indent + 1);
                printf("Default:\n");
                tile_ast_show(node->match_statement.default_option, indent + 2);
                print_indent(indent + 1);
            }
            else {
                // Match statement hasn't a default option 
                for(size_t i = 0; i < node->match_statement.option_count; i++) {
                    tile_ast_show(node->match_statement.options[i], indent + 1);
                } 
            }
            break;

        case AST_OPTION_STATEMENT:
            printf("OptionStmt\n");
            print_indent(indent + 1);
            printf("Condition:\n");
            tile_ast_show(node->option_statement.condition, indent + 2);
            print_indent(indent + 1);
            printf("Statements:\n");
            for(size_t i = 0; i < node->option_statement.statement_count; i++) {
                tile_ast_show(node->option_statement.statements[i], indent + 2);
            }
            break;

        case AST_DEFAULT_STATEMENT:
            printf("DefaultStmt\n");
            print_indent(indent + 1);
            printf("Statements:\n");
            for(size_t i = 0; i < node->default_statement.statement_count; i++) {
                tile_ast_show(node->default_statement.statements[i], indent + 2);
            }
            break;

        case AST_FUNCTION_STATEMENT:
            printf("FuncStmt\n");
            print_indent(indent + 1);
            printf("Arguments:\n");
            for(size_t i = 0; i < node->function_statement.argument_count; i++) {
                tile_ast_show(node->function_statement.arguments[i], indent + 2);
            }
            tile_ast_show(node->function_statement.return_type, indent + 1);
            tile_ast_show(node->function_statement.body, indent + 1);
            break;

        case AST_FUNCTION_ARGUMENT:
            printf("Argument\n");
            break;

        case AST_FUNCTION_RETURN_TYPE:
            printf("Return type:\n");
            print_indent(indent + 1);
            switch (node->return_type.type_name) {
            case PRIM_TYPE_INT:
                printf("int\n");
                break;
            case PRIM_TYPE_FLOAT:
                printf("float\n");
                break;
            default:
                printf("unknown type\n");
                break;
            }
            break;

        case AST_RETURN_STATEMENT:
            printf("Return Statement:\n");
            // print_indent(indent + 1);
            tile_ast_show(node->return_statement.expression, indent + 1);
            break;

        case AST_BLOCK:
            printf("BLOCK\n");
            for(size_t i = 0; i < node->block.statement_count; i++) {
                tile_ast_show(node->block.statements[i], indent + 1);
            }
            break;
        
        default:
            printf("UNKNOWN\n");
            break;
    }
}