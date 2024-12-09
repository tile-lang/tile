package tile;

import gen.antlr.tile.tileParser.AdditiveExpressionContext;
import gen.antlr.tile.tileParser.AndExpressionContext;
import gen.antlr.tile.tileParser.AssignmentExpressionContext;
import gen.antlr.tile.tileParser.CastExpressionContext;
import gen.antlr.tile.tileParser.ConditionalExpressionContext;
import gen.antlr.tile.tileParser.EqualityExpressionContext;
import gen.antlr.tile.tileParser.ExclusiveOrExpressionContext;
import gen.antlr.tile.tileParser.ExpressionContext;
import gen.antlr.tile.tileParser.ExpressionStmtContext;
import gen.antlr.tile.tileParser.FuncCallExpressionContext;
import gen.antlr.tile.tileParser.FuncDefStmtContext;
import gen.antlr.tile.tileParser.InclusiveOrExpressionContext;
import gen.antlr.tile.tileParser.LogicalAndExpressionContext;
import gen.antlr.tile.tileParser.LogicalOrExpressionContext;
import gen.antlr.tile.tileParser.MultiplicativeExpressionContext;
import gen.antlr.tile.tileParser.PrimaryExpressionContext;
import gen.antlr.tile.tileParser.ProgramContext;
import gen.antlr.tile.tileParser.RelationalExpressionContext;
import gen.antlr.tile.tileParser.ShiftExpressionContext;
import gen.antlr.tile.tileParser.UnaryExpressionContext;

import java.util.ArrayList;

import org.antlr.v4.runtime.ParserRuleContext;

import gen.antlr.tile.tileParserBaseVisitor;
import tile.ast.base.Expression;
import tile.ast.expr.AdditativeExpression;
import tile.ast.expr.CastExpression;
import tile.ast.expr.FuncCallExpression;
import tile.ast.expr.MultiplicativeExpression;
import tile.ast.expr.PrimaryExpression;
import tile.ast.expr.RelationalExpression;
import tile.ast.expr.UnaryExpression;
import tile.ast.stmt.FunctionDefinition;
import tile.ast.types.TypeResolver;
import tile.ast.types.TypeResolver.TypeFuncCall;
import tile.ast.types.TypeResolver.TypeInfoBinop;
import tile.ast.types.TypeResolver.TypeInfoBinopBool;
import tile.ast.types.TypeResolver.TypeInfoCast;
import tile.sym.TasmSymbolGenerator;

public class AntlrToExpression extends tileParserBaseVisitor<Expression> {

    @Override
    public Expression visitPrimaryExpression(PrimaryExpressionContext ctx) {
        int count = ctx.getChildCount();
        Expression expr = null;

        ParserRuleContext parent = null;
        String unaryOp = null;
        if (ctx.getParent() instanceof UnaryExpressionContext) {
            parent = ctx.getParent();
            unaryOp = ((UnaryExpressionContext)parent).unaryOperator().getText();
        }

        if (count == 1) { 
            if (ctx.INT_LITERAL() != null) {
                String intLiteral = ctx.INT_LITERAL().getText();
                expr = new PrimaryExpression(unaryOp, intLiteral, "int", false, 0);
            }
            else if (ctx.FLOAT_LITERAL() != null) {
                String floatLiteral = ctx.FLOAT_LITERAL().getText();
                expr = new PrimaryExpression(unaryOp, floatLiteral, "float", false, 0);
            }
            else if (ctx.CHAR_LITERAL() != null) {
                String chrLiteral = ctx.CHAR_LITERAL().getText();
                expr = new PrimaryExpression(unaryOp, chrLiteral, "char", false, 0);
            }
            else if (ctx.BOOL_LITERAL() != null) {
                String boolLiteral = ctx.BOOL_LITERAL().getText();
                if (boolLiteral.equals("true"))
                    expr = new PrimaryExpression(unaryOp, "1", "bool", false, 0);
                else if (boolLiteral.equals("false"))
                    expr = new PrimaryExpression(unaryOp, "0", "bool", false, 0);
            }
            else if (ctx.IDENTIFIER() != null) {
                String identifier = ctx.IDENTIFIER().getText();
                
                ParserRuleContext parentFunc = ctx;
                while (!(parentFunc instanceof FuncDefStmtContext)) {
                    parentFunc = parentFunc.getParent();
                    if (parentFunc instanceof ProgramContext) {
                        parentFunc = null;
                        break;
                    }
                }
                if (parentFunc == null) {
                    int line = ctx.IDENTIFIER().getSymbol().getLine();
                    // FIXME: allow variable def outside functions!
                    System.err.println("ERROR:" + line + ": " + "variable definition outside a function!");
                    return null;
                }
                
                String funcId = ((FuncDefStmtContext)parentFunc).IDENTIFIER().getText();
                String tasmFuncSym = TasmSymbolGenerator.tasmGenFunctionName(funcId);
                FunctionDefinition fds = FunctionDefinition.funcDefSymbols.get(tasmFuncSym);

                String tasmVarSym = TasmSymbolGenerator.tasmGenVariableName(funcId, identifier);

                if (!fds.variableSymbols.containsKey(tasmVarSym)) {
                    int line = ((FuncDefStmtContext)parentFunc).IDENTIFIER().getSymbol().getLine();
                    System.err.println("ERROR:" + line + ": variable " + "'" + identifier + "' is not defined before use!");
                }

                int tasmIdx = fds.variableSymbols.get(tasmVarSym).getTasmIdx();
                String varType = fds.variableSymbols.get(tasmVarSym).getType();

                expr = new PrimaryExpression(unaryOp, identifier, varType, true, tasmIdx);
            }
        } else if (count == 3 && ctx.getChild(0).getText().equals("(") && ctx.getChild(2).getText().equals(")")) {
            // Parentheses case: Visit the inner expression
            Expression innerExpr = visit(ctx.expression());
            if (unaryOp != null) {
                // Apply the unary operator to the inner expression
                expr = new UnaryExpression(unaryOp, innerExpr);
            } else {
                expr = innerExpr;
            }
        } else {
            System.out.println("TODO not reachable!");
        }
        return expr;
    }

    @Override
    public Expression visitAdditiveExpression(AdditiveExpressionContext ctx) {
        // If there's no operator, directly visit the single child (multiplicativeExpression).
        if (ctx.multiplicativeExpression().size() == 1) {
            return visit(ctx.multiplicativeExpression(0));
        }

        // Otherwise, process the operator and operands.
        Expression left = visit(ctx.multiplicativeExpression(0)); // The first operand.
        for (int i = 1; i < ctx.multiplicativeExpression().size(); i++) {
            // Get the operator (+ or -).
            String operator = ctx.getChild((i * 2) - 1).getText(); // Operators are at odd indices.

            // Visit the right operand.
            Expression right = visit(ctx.multiplicativeExpression(i));

            String lhs_type = left.getType();
            String rhs_type = right.getType();;
            TypeInfoBinop type = TypeResolver.resolveBinopNumericType(lhs_type, rhs_type);
            left = new AdditativeExpression(left, operator, right, type);
        }
        return left;
    }

    @Override
    public Expression visitAndExpression(AndExpressionContext ctx) {
        // TODO Auto-generated method stub
        return super.visitAndExpression(ctx);
    }

    @Override
    public Expression visitAssignmentExpression(AssignmentExpressionContext ctx) {
        // TODO Auto-generated method stub
        return super.visitAssignmentExpression(ctx);
    }

    @Override
    public Expression visitCastExpression(CastExpressionContext ctx) {
        if (ctx.typeName() == null) {
            if (ctx.primaryExpression() != null) {
                return visit(ctx.primaryExpression());
            } else if (ctx.funcCallExpression() != null) {
                return visit(ctx.funcCallExpression());
            } else if (ctx.unaryExpression() != null) {
                return visit(ctx.unaryExpression());
            }
        }
        String cast_type = ctx.typeName().getText();
        String expr_type = null;
        Expression expr = null;
        if (ctx.primaryExpression() != null) {
            expr = visit(ctx.primaryExpression());
        } else if (ctx.funcCallExpression() != null) {
            expr = visit(ctx.funcCallExpression());
        } else if (ctx.unaryExpression() != null) {
            expr = visit(ctx.unaryExpression());
        }
        expr_type = expr.getType();

        TypeInfoCast type = TypeResolver.resolveCastType(expr_type, cast_type);
        Expression castExpr = new CastExpression(expr, type);
        return castExpr;
    }

    @Override
    public Expression visitConditionalExpression(ConditionalExpressionContext ctx) {
        // TODO Auto-generated method stub
        return super.visitConditionalExpression(ctx);
    }

    @Override
    public Expression visitEqualityExpression(EqualityExpressionContext ctx) {
        // TODO Auto-generated method stub
        return super.visitEqualityExpression(ctx);
    }

    @Override
    public Expression visitExclusiveOrExpression(ExclusiveOrExpressionContext ctx) {
        // TODO Auto-generated method stub
        return super.visitExclusiveOrExpression(ctx);
    }

    @Override
    public Expression visitExpression(ExpressionContext ctx) {
        // TODO Auto-generated method stub
        return super.visitExpression(ctx);
    }

    @Override
    public Expression visitExpressionStmt(ExpressionStmtContext ctx) {
        // TODO Auto-generated method stub
        return super.visitExpressionStmt(ctx);
    }

    @Override
    public Expression visitFuncCallExpression(FuncCallExpressionContext ctx) {
        ArrayList<Expression> arg_exprs = new ArrayList<>();
        String funcId = ctx.IDENTIFIER().getText();
        TypeFuncCall type = new TypeFuncCall();
        int line = ctx.IDENTIFIER().getSymbol().getLine();

        String tasmFuncSym = TasmSymbolGenerator.tasmGenFunctionName(funcId);

        try {
            type.result_type = FunctionDefinition.funcDefSymbols.get(tasmFuncSym).getReturnType();
        } catch (NullPointerException e) {
            System.err.println("ERROR:" + line + ": function " + funcId + " is not defined before called.");
        }

        int arg_size = FunctionDefinition.funcDefSymbols.get(tasmFuncSym).getArgs().size();

        if (arg_size != ctx.expression().size()) {
            System.err.println("ERROR:" + line + ": function call" + funcId + " doesn't match by argument count, expected " + arg_size + " but got " +ctx.expression().size());
            return null;
        }

        for (int i = 0; i < ctx.expression().size(); i++) {
            Expression expr = visit(ctx.expression(i));
            arg_exprs.add(expr);
        }

        FuncCallExpression fce = new FuncCallExpression(funcId, arg_exprs, type, false);

        return fce;
    }

    @Override
    public Expression visitInclusiveOrExpression(InclusiveOrExpressionContext ctx) {
        // TODO Auto-generated method stub
        return super.visitInclusiveOrExpression(ctx);
    }

    @Override
    public Expression visitLogicalAndExpression(LogicalAndExpressionContext ctx) {
        // TODO Auto-generated method stub
        return super.visitLogicalAndExpression(ctx);
    }

    @Override
    public Expression visitLogicalOrExpression(LogicalOrExpressionContext ctx) {
        // TODO Auto-generated method stub
        return super.visitLogicalOrExpression(ctx);
    }

    @Override
    public Expression visitMultiplicativeExpression(MultiplicativeExpressionContext ctx) {
        // If there's no operator, directly visit the single child (castExpression).
        if (ctx.castExpression().size() == 1) {
            return visit(ctx.castExpression(0));
        }

        // Otherwise, process the operator and operands.
        Expression left = visit(ctx.castExpression(0)); // The first operand.
        for (int i = 1; i < ctx.castExpression().size(); i++) {
            // Get the operator (* or /).
            String operator = ctx.getChild((i * 2) - 1).getText(); // Operators are at odd indices.

            // Visit the right operand.
            Expression right = visit(ctx.castExpression(i));

            String lhs_type = left.getType();
            String rhs_type = right.getType();
            TypeInfoBinop type = TypeResolver.resolveBinopNumericType(lhs_type, rhs_type);
            left = new MultiplicativeExpression(left, operator, right, type);
        }
        return left;
    }

    @Override
    public Expression visitRelationalExpression(RelationalExpressionContext ctx) {
        // If there's no operator, directly visit the single child (castExpression).
        if (ctx.shiftExpression().size() == 1) {
            return visit(ctx.shiftExpression(0));
        }

        // Otherwise, process the operator and operands.
        Expression left = visit(ctx.shiftExpression(0)); // The first operand.
        for (int i = 1; i < ctx.shiftExpression().size(); i++) {
            // Get the operator (* or /).
            String operator = ctx.getChild((i * 2) - 1).getText(); // Operators are at odd indices.

            // Visit the right operand.
            Expression right = visit(ctx.shiftExpression(i));

            String lhs_type = left.getType();
            String rhs_type = right.getType();
            TypeInfoBinopBool type = TypeResolver.resolveBinopBooleanType(lhs_type, rhs_type);
            left = new RelationalExpression(left, operator, right, type);
        }
        return left;
    }

    @Override
    public Expression visitShiftExpression(ShiftExpressionContext ctx) {
        // TODO Auto-generated method stub
        return super.visitShiftExpression(ctx);
    }

    @Override
    public Expression visitUnaryExpression(UnaryExpressionContext ctx) {
        Expression expr = null;
        if (ctx.primaryExpression() != null) {
            if (ctx.unaryOperator() != null) {
                String unaryOp = ctx.unaryOperator().getText();
                expr = visit(ctx.primaryExpression());

                if (unaryOp.equals("+") || unaryOp.equals("-")) {
                    String type = expr.getType();
                    if (!(type.equals("int") || type.equals("float"))) {
                        System.err.println("ERROR: '+' and '-' prefixes cannot go before any non-numeric type!");
                    }
                }
            }
        }
        // TODO: handle IDENTIFIER and ++ | -- operators !!!

        return expr;
    }

    
}
