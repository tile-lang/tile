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
import gen.antlr.tile.tileParser.VariableAssignmentContext;
import gen.antlr.tile.tileParser.VariableDeclerationContext;
import gen.antlr.tile.tileParser.VariableDefinitionContext;
import gen.antlr.tile.tileParser.WhileStmtContext;
import gen.antlr.tile.tileParserBaseVisitor;
import tile.ast.base.*;
import tile.ast.stmt.BlockStmt;
import tile.ast.stmt.ExpressionStmt;
import tile.ast.stmt.FunctionDefinition;
import tile.ast.stmt.FunctionDefinition.FuncArg;
import tile.ast.stmt.IfStmt;
import tile.ast.stmt.ReturnStmt;
import tile.ast.stmt.VariableDefinition;
import tile.ast.stmt.BlockStmt.BlockType;
import tile.ast.types.TypeResolver;
import tile.ast.types.TypeResolver.TypeFuncCall;
import tile.ast.types.TypeResolver.TypeInfoRetStmt;
import tile.ast.types.TypeResolver.TypeInfoVariableDef;
import tile.sym.TasmSymbolGenerator;

public class AntlrToStatement extends tileParserBaseVisitor<Statement> {

    @Override
    public Statement visitBlockStmt(BlockStmtContext ctx) {
        BlockType blockType = BlockType.Regular;

        if (ctx.getParent() instanceof IfStmtContext) {
            blockType = BlockType.IfBlock;
        } else if (ctx.getParent() instanceof FuncDefStmtContext) {
            blockType = BlockType.FuncDefBlock;
        } else if (ctx.getParent() instanceof WhileStmtContext) {
            blockType = BlockType.WhileLoopBlock;
        } else if (ctx.getParent() instanceof ForStmtContext) {
            blockType = BlockType.ForLoopBlock;
        }

        Statement blockStmt = new BlockStmt(blockType);
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
        if ((ctx.getParent() instanceof ReturnStmtContext) || (ctx.getChild(0).getChild(0) instanceof FuncCallExpressionContext) || ctx.getParent() instanceof VariableDefinitionContext) {
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
        
        // just add decleration part, it is enough
        fds = new FunctionDefinition(funcId, args, return_type, null);


        // add the parameter variables to function's variableSymbols table
        for (int i = 0; i < args.size(); i++) {
            String varId = args.get(i).getArgId();
            String varType = args.get(i).getType();
            TypeInfoVariableDef typeInfo = TypeResolver.resolveVariableDefForFunctionArgs(varType);

            VariableDefinition vd = new VariableDefinition(typeInfo, varId, fds);
            String tasmVarSym = TasmSymbolGenerator.tasmGenVariableName(funcId, varId);
            vd.setTasmIdx(fds.getTasmVarIdx());
            fds.variableSymbols.put(tasmVarSym, vd);
        }


        // add to the hash table to see if it is defined when call the function
        String tasmFuncSym = TasmSymbolGenerator.tasmGenFunctionName(funcId);

        // check if it variable with same name is already defined
        if (FunctionDefinition.funcDefSymbols.containsKey(tasmFuncSym)) {
            int line = ctx.IDENTIFIER().getSymbol().getLine();
            System.err.println("ERROR:" + line + ": function '" + funcId + "' is already defined.");
        }
        FunctionDefinition.funcDefSymbols.put(tasmFuncSym, fds);

        BlockStmt block = null;
        block = (BlockStmt)visit(ctx.getChild(ctx.getChildCount() - 1));

        fds = new FunctionDefinition(funcId, args, return_type, block);
        // set that functionDef with same functionDef but with it has the blockStmt version
        // ^^^^^^^^^^^^ this makes variableSymbols of each function empty again!!!
        // FunctionDefinition.funcDefSymbols.put(tasmFuncSym, fds);

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
    public Statement visitWhileStmt(WhileStmtContext ctx) {
        // TODO Auto-generated method stub
        return super.visitWhileStmt(ctx);
    }

    @Override
    public Statement visitVariableAssignment(VariableAssignmentContext ctx) {
        // TODO Auto-generated method stub
        return super.visitVariableAssignment(ctx);
    }

    @Override
    public Statement visitVariableDecleration(VariableDeclerationContext ctx) {
        // TODO Auto-generated method stub
        return super.visitVariableDecleration(ctx);
    }

    @Override
    public Statement visitVariableDefinition(VariableDefinitionContext ctx) {
        String type = ctx.typeName().getText();
        String varId = ctx.IDENTIFIER().getText();
        Statement exprStmt = visit(ctx.expressionStmt());

        // find the funcId
        ParserRuleContext parent = ctx;
        while (!(parent instanceof FuncDefStmtContext)) {
            parent = parent.getParent();
            if (parent instanceof ProgramContext) {
                parent = null;
                break;
            }
        }
        String funcId = "";
        if (parent != null) {
            funcId = ((FuncDefStmtContext)parent).IDENTIFIER().getText();
        } else {
            // FIXME: allow user to create global variables...
            System.out.println("ERROR: visitVariableDecleration parent is null!");
        }


        // find the function
        String tasmFuncSym = TasmSymbolGenerator.tasmGenFunctionName(funcId);
        FunctionDefinition fd = FunctionDefinition.funcDefSymbols.get(tasmFuncSym);

        String exprType = ((ExpressionStmt)exprStmt).getType();
        TypeInfoVariableDef typeInfo = TypeResolver.resolveVariableDefType(type, exprType);

        VariableDefinition vd = new VariableDefinition(typeInfo, varId, exprStmt);
        vd.setTasmIdx(fd.getTasmVarIdx());
        
        // put the variable to function's hashtable
        String tasmVarSym = TasmSymbolGenerator.tasmGenVariableName(funcId, varId);

        // check if it variable with same name is already defined
        if (fd.variableSymbols.containsKey(tasmVarSym)) {
            int line = ctx.IDENTIFIER().getSymbol().getLine();
            System.err.println("ERROR:" + line + ": variable '" + varId + "' is already defined in function '" + funcId + "'.");
        }
        fd.variableSymbols.put(tasmVarSym, vd);

        return vd;
    }

    
}
