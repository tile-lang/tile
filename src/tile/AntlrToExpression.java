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
import gen.antlr.tile.tileParser.InclusiveOrExpressionContext;
import gen.antlr.tile.tileParser.LogicalAndExpressionContext;
import gen.antlr.tile.tileParser.LogicalOrExpressionContext;
import gen.antlr.tile.tileParser.MultiplicativeExpressionContext;
import gen.antlr.tile.tileParser.PrimaryExpressionContext;
import gen.antlr.tile.tileParser.RelationalExpressionContext;
import gen.antlr.tile.tileParser.ShiftExpressionContext;
import gen.antlr.tile.tileParser.UnaryExpressionContext;
import gen.antlr.tile.tileParserBaseVisitor;
import tile.ast.base.Expression;
import tile.ast.expr.AdditativeExpression;
import tile.ast.expr.CastExpression;
import tile.ast.expr.MultiplicativeExpression;
import tile.ast.expr.PrimaryExpression;
import tile.ast.types.TypeReslover;
import tile.ast.types.TypeReslover.TypeInfoBinop;
import tile.ast.types.TypeReslover.TypeInfoCast;

public class AntlrToExpression extends tileParserBaseVisitor<Expression> {

    @Override
    public Expression visitPrimaryExpression(PrimaryExpressionContext ctx) {
        int count = ctx.getChildCount();
        Expression expr = null;
        if (count == 1) { 
            if (ctx.INT_LITERAL() != null) {
                String intLiteral = ctx.INT_LITERAL().getText();
                expr = new PrimaryExpression(intLiteral, "int");
            }
            if (ctx.FLOAT_LITERAL() != null) {
                String floatLiteral = ctx.FLOAT_LITERAL().getText();
                expr = new PrimaryExpression(floatLiteral, "float");
            }
            if (ctx.CHAR_LITERAL() != null) {
                String chrLiteral = ctx.CHAR_LITERAL().getText();
                expr = new PrimaryExpression(chrLiteral, "char");
            }
            if (ctx.BOOL_LITERAL() != null) {
                String boolLiteral = ctx.BOOL_LITERAL().getText();
                if (boolLiteral == "true")
                    expr = new PrimaryExpression("1", "bool");
                else if (boolLiteral == "false")
                    expr = new PrimaryExpression("0", "bool");
            }
            if (ctx.IDENTIFIER() != null) {
                String identifier = ctx.IDENTIFIER().getText();
                // FIXME: find the correct type from a lookup table!
                expr = new PrimaryExpression(identifier, "Object");
            }
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
            TypeInfoBinop type = TypeReslover.resolveBinopNumericType(lhs_type, rhs_type);
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
            }
        }
        String cast_type = ctx.typeName().getText();
        String expr_type = null;
        Expression expr = null;
        if (ctx.primaryExpression() != null) {
            expr = visit(ctx.primaryExpression());
        } else if (ctx.funcCallExpression() != null) {
            expr = visit(ctx.funcCallExpression());
        }
        expr_type = expr.getType();

        TypeInfoCast type = TypeReslover.resolveCastType(expr_type, cast_type);
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
        // TODO Auto-generated method stub
        return super.visitFuncCallExpression(ctx);
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
            TypeInfoBinop type = TypeReslover.resolveBinopNumericType(lhs_type, rhs_type);
            left = new MultiplicativeExpression(left, operator, right, type);
        }
        return left;
    }

    @Override
    public Expression visitRelationalExpression(RelationalExpressionContext ctx) {
        // TODO Auto-generated method stub
        return super.visitRelationalExpression(ctx);
    }

    @Override
    public Expression visitShiftExpression(ShiftExpressionContext ctx) {
        // TODO Auto-generated method stub
        return super.visitShiftExpression(ctx);
    }

    @Override
    public Expression visitUnaryExpression(UnaryExpressionContext ctx) {
        // TODO Auto-generated method stub
        return super.visitUnaryExpression(ctx);
    }

    
}
