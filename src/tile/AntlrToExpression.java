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
import tile.ast.expr.PrimaryExpression;

public class AntlrToExpression extends tileParserBaseVisitor<Expression> {

    @Override
    public Expression visitPrimaryExpression(PrimaryExpressionContext ctx) {
        //TODO: upgrade this.
        String intlit = ctx.INT_LITERAL().getText();
        Expression expr = new PrimaryExpression(intlit);
        return expr;
    }

    @Override
    public Expression visitAdditiveExpression(AdditiveExpressionContext ctx) {
        // TODO Auto-generated method stub
        return super.visitAdditiveExpression(ctx);
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
        // TODO Auto-generated method stub
        return super.visitCastExpression(ctx);
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
        // TODO Auto-generated method stub
        return super.visitMultiplicativeExpression(ctx);
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
