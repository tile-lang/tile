package tile;

import java.util.ArrayList;

import gen.antlr.tile.tileParser.BlockStmtContext;
import gen.antlr.tile.tileParser.ExpressionStmtContext;
import gen.antlr.tile.tileParser.ForStmtContext;
import gen.antlr.tile.tileParser.FuncDefStmtContext;
import gen.antlr.tile.tileParser.IfStmtContext;
import gen.antlr.tile.tileParser.LoopStmtContext;
import gen.antlr.tile.tileParser.ReturnStmtContext;
import gen.antlr.tile.tileParser.SelectionStmtContext;
import gen.antlr.tile.tileParser.VariableStmtContext;
import gen.antlr.tile.tileParser.WhileStmtContext;
import gen.antlr.tile.tileParserBaseVisitor;
import tile.ast.base.*;
import tile.ast.stmt.BlockStmt;
import tile.ast.stmt.ExpressionStmt;
import tile.ast.stmt.FunctionDefinition;
import tile.ast.stmt.FunctionDefinition.FuncArg;
import tile.ast.stmt.IfStmt;

public class AntlrToStatement extends tileParserBaseVisitor<Statement> {

    @Override
    public Statement visitBlockStmt(BlockStmtContext ctx) {
        Statement blockStmt = new BlockStmt();
        if (ctx.statements() == null) {
            return blockStmt;
        }
        
        for (int i = 0; i < ctx.statements().statement().size(); i++) {
            Statement stmt = visit(ctx.statements().statement(i));
            ((BlockStmt)blockStmt).addStatement(stmt);
        }
        return blockStmt;
    }

    @Override
    public Statement visitExpressionStmt(ExpressionStmtContext ctx) {
        AntlrToExpression exprVisitor = new AntlrToExpression();
        Expression expr = exprVisitor.visit(ctx.expression());
        Statement expressionStatement = new ExpressionStmt(expr);
        return expressionStatement;
    }

    @Override
    public Statement visitForStmt(ForStmtContext ctx) {
        // TODO Auto-generated method stub
        return super.visitForStmt(ctx);
    }

    @Override
    public Statement visitFuncDefStmt(FuncDefStmtContext ctx) {
        String funcId = ctx.IDENTIFIER().getText();
        ArrayList<FuncArg> args = new ArrayList<>();

        FunctionDefinition fds = null;

        for (int i = 1; i < ctx.argument().size(); i++) {
            FuncArg arg = new FuncArg(
                ctx.argument(i).typeName().getText(),
                ctx.argument(i).IDENTIFIER().getText(),
                false
            );
            args.add(arg);
        }
        
        BlockStmt block = new BlockStmt();
        block = (BlockStmt)visit(ctx.getChild(ctx.getChildCount() - 1));


        fds = new FunctionDefinition(funcId, args, block);
        return fds;
    }

    @Override
    public Statement visitIfStmt(IfStmtContext ctx) {
        AntlrToExpression exprVisitor = new AntlrToExpression();
        Expression expr = exprVisitor.visit(ctx.expression());
        Statement stmt = null;
        Statement altarnateStmt = null;

        stmt = visit(ctx.blockStmt(0));
        
        if (ctx.KW_ELSE() != null) {

            if (ctx.blockStmt(1) != null)
                    altarnateStmt = visit(ctx.blockStmt(1));
            else if (ctx.ifStmt() != null)
                altarnateStmt = visit(ctx.ifStmt());
        }

        Statement ifStmt = new IfStmt(expr, stmt, altarnateStmt);

        return ifStmt;        
    }

    @Override
    public Statement visitLoopStmt(LoopStmtContext ctx) {
        // TODO Auto-generated method stub
        return super.visitLoopStmt(ctx);
    }

    @Override
    public Statement visitReturnStmt(ReturnStmtContext ctx) {
        // TODO Auto-generated method stub
        return super.visitReturnStmt(ctx);
    }

    @Override
    public Statement visitSelectionStmt(SelectionStmtContext ctx) {
        // TODO Auto-generated method stub
        return super.visitSelectionStmt(ctx);
    }

    @Override
    public Statement visitVariableStmt(VariableStmtContext ctx) {
        // TODO Auto-generated method stub
        return super.visitVariableStmt(ctx);
    }

    @Override
    public Statement visitWhileStmt(WhileStmtContext ctx) {
        // TODO Auto-generated method stub
        return super.visitWhileStmt(ctx);
    }

    
}
