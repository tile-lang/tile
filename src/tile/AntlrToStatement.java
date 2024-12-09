package tile;

import java.util.ArrayList;

import org.antlr.v4.runtime.ParserRuleContext;

import gen.antlr.tile.tileParser.BlockStmtContext;
import gen.antlr.tile.tileParser.ExpressionStmtContext;
import gen.antlr.tile.tileParser.ForStmtContext;
import gen.antlr.tile.tileParser.FuncCallExpressionContext;
import gen.antlr.tile.tileParser.FuncDefStmtContext;
import gen.antlr.tile.tileParser.IfStmtContext;
import gen.antlr.tile.tileParser.LoopStmtContext;
import gen.antlr.tile.tileParser.ProgramContext;
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
import tile.ast.stmt.ReturnStmt;
import tile.ast.types.TypeResolver;
import tile.ast.types.TypeResolver.TypeFuncCall;
import tile.ast.types.TypeResolver.TypeInfoRetStmt;

public class AntlrToStatement extends tileParserBaseVisitor<Statement> {

    @Override
    public Statement visitBlockStmt(BlockStmtContext ctx) {
        Statement blockStmt = new BlockStmt();
        if (ctx.localStatements() == null) {
            return blockStmt;
        }
        
        for (int i = 0; i < ctx.localStatements().localStatement().size(); i++) {
            Statement stmt = visit(ctx.localStatements().localStatement(i));
            ((BlockStmt)blockStmt).addStatement(stmt);
        }
        return blockStmt;
    }

    @Override
    public Statement visitExpressionStmt(ExpressionStmtContext ctx) {
        // We need to eliminate code generation for ExpressionStmtContext whose parents are NOT ReturnStmtContext.
        // this will eliminate code lines like: "5;" or "3 + 8 * 2;" to generate 'push' and 'binop(mult, add etc.)' instructions.
        boolean generate = false;
        if ((ctx.getParent() instanceof ReturnStmtContext) || (ctx.getChild(0).getChild(0) instanceof FuncCallExpressionContext)) {
            generate = true;
        }
        AntlrToExpression exprVisitor = new AntlrToExpression();

        // Allow empty semicolons as statements like:
        // ;
        Expression expr = null;
        if (ctx.expression() == null) {
            generate = false;
        } else {
            expr = exprVisitor.visit(ctx.expression());
        }
        // System.out.println("debug: expr: " + expr);
        Statement expressionStatement = new ExpressionStmt(expr, generate);
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
        TypeFuncCall return_type = new TypeFuncCall();
        return_type.result_type = ctx.typeName().getText();
        FunctionDefinition fds = null;

        for (int i = 0; i < ctx.argument().size(); i++) {
            FuncArg arg = new FuncArg(
                ctx.argument(i).typeName().getText(),
                ctx.argument(i).IDENTIFIER().getText(),
                false
            );
            args.add(arg);
        }
        
        BlockStmt block = new BlockStmt();
        block = (BlockStmt)visit(ctx.getChild(ctx.getChildCount() - 1));


        fds = new FunctionDefinition(funcId, args, return_type, block);
        return fds;
    }

    @Override
    public Statement visitIfStmt(IfStmtContext ctx) {
        AntlrToExpression exprVisitor = new AntlrToExpression();
        Expression expr = exprVisitor.visit(ctx.expression());
        Statement stmt = null;
        Statement altarnateStmt = null;

        if (!(expr.getType().equals("bool"))) {
            int line = ctx.KW_IF().getSymbol().getLine();
            System.err.println("WARNING:" + line + ": if condition expression type should be a bool type!");
        }

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
        ParserRuleContext parent = ctx;

        while (!(parent instanceof FuncDefStmtContext)) {
            parent = parent.getParent();
            if (parent instanceof ProgramContext) {
                parent = null;
                break;
            }
        }
        if (parent == null) {
            int line = ctx.KW_RETURN().getSymbol().getLine();
            System.err.println("ERROR:" + line + ": " + "return statement cannot be used outside a function definiton!");
            return null;
        }

        Statement exprStmt = visit(ctx.expressionStmt());
        
        String func_ret_type = ((FuncDefStmtContext)parent).typeName().getText();
        String expr_ret_type = ((ExpressionStmt)exprStmt).getType();

        TypeInfoRetStmt rtype = TypeResolver.resolveRetStmtType(expr_ret_type, func_ret_type);

        ReturnStmt rs = new ReturnStmt(exprStmt, rtype);
        
        return rs;
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
