package tile;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.ParserRuleContext;

import gen.antlr.tile.tileParser.BlockStmtContext;
import gen.antlr.tile.tileParser.ExpressionStmtContext;
import gen.antlr.tile.tileParser.ForStmtContext;
import gen.antlr.tile.tileParser.FuncCallExpressionContext;
import gen.antlr.tile.tileParser.FuncDefStmtContext;
import gen.antlr.tile.tileParser.IfStmtContext;
import gen.antlr.tile.tileParser.LoopStmtContext;
import gen.antlr.tile.tileParser.NativeFuncDeclStmtContext;
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
import tile.ast.stmt.NativeFunctionDecl;
import tile.ast.stmt.ReturnStmt;
import tile.ast.stmt.VariableAssignment;
import tile.ast.stmt.VariableDefinition;
import tile.ast.stmt.WhileStmt;
import tile.ast.stmt.ForStmt;
import tile.ast.types.TypeResolver;
import tile.ast.types.TypeResolver.TypeFuncCall;
import tile.ast.types.TypeResolver.TypeInfoArray;
import tile.ast.types.TypeResolver.TypeInfoRetStmt;
import tile.ast.types.TypeResolver.TypeInfoVariableDef;
import tile.sym.TasmSymbolGenerator;

public class AntlrToStatement extends tileParserBaseVisitor<Statement> {

    @Override
    public Statement visitBlockStmt(BlockStmtContext ctx) {
        BlockStmt blockStmt = null;

        ParserRuleContext parent = ctx.getParent();
        if (parent instanceof IfStmtContext) {
            blockStmt = new BlockStmt(BlockStmt.BlockType.IfBlock);
        } else if (parent instanceof FuncDefStmtContext) {
            blockStmt = new BlockStmt(BlockStmt.BlockType.FuncDefBlock);
        } else if (parent instanceof WhileStmtContext) {
            blockStmt = new BlockStmt(BlockStmt.BlockType.WhileLoopBlock);
        } else if (parent instanceof ForStmtContext) {
            blockStmt = new BlockStmt(BlockStmt.BlockType.ForLoopBlock);
        } else {
            blockStmt = new BlockStmt(BlockStmt.BlockType.Regular);
        }

        FunctionDefinition func = null;
        int variableTasmId = 0;
        ParserRuleContext parentFunc = ctx.getParent();
        while (!(parentFunc instanceof FuncDefStmtContext)) {
            parentFunc = parentFunc.getParent();
            if (parentFunc instanceof ProgramContext) {
                parentFunc = null;
                break;
            }
        }
        if (parentFunc == null) {
            // FIXME: allow variable def outside functions!
            System.err.println("FIXME Error:::: Program!");
        }

        String funcId = ((FuncDefStmtContext)parentFunc).IDENTIFIER().getText();
        String tasmFuncSym = TasmSymbolGenerator.tasmGenFunctionName(funcId);
        func = Program.funcDefSymbols.get(tasmFuncSym);
        
        Program.blockStack.add(blockStmt);
        if (parent instanceof FuncDefStmtContext) {
            func.setBlockStmt(((BlockStmt)blockStmt));
            // add the parameter variables to block's variableSymbols table
            for (int i = 0; i < func.getArgs().size(); i++) {
                String varId = func.getArgs().get(i).getArgId();
                String varType = func.getArgs().get(i).getType();

                TypeInfoVariableDef typeInfo = TypeResolver.resolveVariableDefForFunctionArgs(varType);
                VariableDefinition vd = new VariableDefinition(typeInfo, varId, null);
                
                variableTasmId = func.getTasmVarIdx();
                
                vd.setTasmIdx(variableTasmId);
                int blockId = blockStmt.getBlockId();
                String tasmVarSym = TasmSymbolGenerator.tasmGenVariableName(blockId, varId);
                
                if (blockStmt.variableSymbols.containsKey(tasmVarSym)) {
                    int line = ((FuncDefStmtContext)parentFunc).IDENTIFIER().getSymbol().getLine();
                    System.err.println("ERROR:" + line + ": variable " + "'" + varId + "' is already defined in the same scope!");
                }
                blockStmt.variableSymbols.put(tasmVarSym, vd);
            }
        }


        
        if (ctx.localStatements() == null) {
            Program.blockStack.remove(Program.blockStack.size() - 1);
            return blockStmt;
        }
        
        
        for (int i = 0; i < ctx.localStatements().localStatement().size(); i++) {
            Statement stmt = visit(ctx.localStatements().localStatement(i));
            if (stmt instanceof VariableDefinition) {
                variableTasmId = func.getTasmVarIdx();
                ((VariableDefinition)stmt).setTasmIdx(variableTasmId);

                String varId = ((VariableDefinition)stmt).getVarId();
                int blockId = blockStmt.getBlockId();
                String tasmVarSym = TasmSymbolGenerator.tasmGenVariableName(blockId, varId);

                if (blockStmt.variableSymbols.containsKey(tasmVarSym)) {
                    int line = ((FuncDefStmtContext)parentFunc).IDENTIFIER().getSymbol().getLine();
                    System.err.println("ERROR:" + line + ": variable " + "'" + varId + "' is already defined in the same scope!");
                }

                blockStmt.variableSymbols.put(tasmVarSym, ((VariableDefinition)stmt));
            }
            // if (stmt instanceof BlockStmt) {
            //     blockStmt.childBlocks.add((BlockStmt)stmt);
            // }
            blockStmt.addStatement(stmt);
        }
        Program.blockStack.remove(Program.blockStack.size() - 1);
        return blockStmt;
    }

    @Override
    public Statement visitExpressionStmt(ExpressionStmtContext ctx) {
        // We need to eliminate code generation for ExpressionStmtContext whose parents are NOT ReturnStmtContext.
        // this will eliminate code lines like: "5;" or "3 + 8 * 2;" to generate 'push' and 'binop(mult, add etc.)' instructions.
        boolean generate = false;
        if ((ctx.getParent() instanceof ReturnStmtContext) || (ctx.getChild(0).getChild(0) instanceof FuncCallExpressionContext) || ctx.getParent() instanceof VariableDefinitionContext || ctx.getParent() instanceof VariableAssignmentContext) {
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
        Statement init = null;
        if (ctx.forInitial() != null) {
            init = visit(ctx.forInitial());

            // Store the for-loop variable in the scope
            if (init instanceof VariableDefinition) {
                VariableDefinition varDef = (VariableDefinition) init;
                int blockId = Program.blockStack.get(Program.blockStack.size() - 1).getBlockId();
                String tasmVarSym = TasmSymbolGenerator.tasmGenVariableName(blockId, varDef.getVarId());

                // Prevent redefinition
                if (Program.blockStack.get(Program.blockStack.size() - 1).variableSymbols.containsKey(tasmVarSym)) {
                    System.err.println("ERROR: Variable '" + varDef.getVarId() + "' is already defined in the same scope!");
                } else {
                    Program.blockStack.get(Program.blockStack.size() - 1).variableSymbols.put(tasmVarSym, varDef);
                }
            }
        }

        // Extract loop expression statement (condition)
        AntlrToExpression exprVisitor = new AntlrToExpression();
        Expression condition = null;
        if (ctx.expressionStmt() != null) {
            condition = exprVisitor.visit(ctx.expressionStmt().expression());
        }

        // Extract update statement
        Statement update = null;
        if (ctx.forUpdate() != null) {
            update = visit(ctx.forUpdate());
        }

        // Extract loop body
        Statement body = visit(ctx.blockStmt());

        return new ForStmt(init, condition, update, body);
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
        String tasmFuncSym = TasmSymbolGenerator.tasmGenFunctionName(funcId);
        Program.funcDefSymbols.put(tasmFuncSym, fds);

        // visit block statement
        // it will set itself to function
        visit(ctx.getChild(ctx.getChildCount() - 1));

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
        AntlrToExpression exprVisitor = new AntlrToExpression();
        Expression expr = exprVisitor.visit(ctx.expression());
        Statement stmt = null;

        if (!(expr.getType().equals("bool"))) {
            int line = ctx.KW_WHILE().getSymbol().getLine();
            System.err.println("WARNING:" + line + ": while condition expression type should be a bool type!");
        }

        stmt = visit(ctx.blockStmt());

        Statement whileStmt = new WhileStmt(expr, stmt);

        return whileStmt;
    }

    @Override
    public Statement visitVariableAssignment(VariableAssignmentContext ctx) {
        String varId = "";
        if (ctx.arrayIndexAccessorSetter() != null) {
            varId = ctx.arrayIndexAccessorSetter().IDENTIFIER().getText();
        } else {
            varId = ctx.IDENTIFIER().getText();
        }

        StringBuilder varType = new StringBuilder();
        int tasmIdx = -1;
        try {
            tasmIdx = TasmSymbolGenerator.identifierScopeFind(varId, varType);
        } catch (Exception e) {
            int line = -1;
            if (ctx.arrayIndexAccessorSetter() != null) {
                line = ctx.arrayIndexAccessorSetter().IDENTIFIER().getSymbol().getLine();
            } else {
                line = ctx.IDENTIFIER().getSymbol().getLine();
            }
            System.err.println("ERROR:" + line + ": variable " + "'" + varId + "' is not defined before assignment!");
        }

        String assignmentOperator = ctx.assignmentOperator().getText();
        Statement exprStmt = visit(ctx.expressionStmt());

        // get the right handside type
        String exprType = ((ExpressionStmt)exprStmt).getType();


        // resolve left handside's dimension if it was an array
        String varTypeStr = varType.toString();
        List<Expression> exprs = null;
        TypeInfoVariableDef typeInfo = null;
        if (ctx.arrayIndexAccessorSetter() != null) {
            int dim = ctx.arrayIndexAccessorSetter().arrayIndexSpecifier().size();
            exprs = new ArrayList<>();
            AntlrToExpression exprVisitor = new AntlrToExpression();
            for (int i = 0; i < dim; i++) {
                Expression expr = exprVisitor.visit(ctx.arrayIndexAccessorSetter().arrayIndexSpecifier(i).expression());
                if (!TypeResolver.isIntType(expr.getType())) {
                    int line = ctx.arrayIndexAccessorSetter().IDENTIFIER().getSymbol().getLine();
                    System.err.println("ERROR:" + line + ": Array index specifier setter must be 'int' type!");
                }
                exprs.add(expr);
            }
            typeInfo = TypeResolver.resolveVariableDefArrayType(varTypeStr, exprType, dim);
        } else {
            typeInfo = TypeResolver.resolveVariableDefType(varTypeStr, exprType);
        }

        VariableAssignment va = new VariableAssignment(typeInfo, varId, assignmentOperator, exprs, exprStmt, tasmIdx);

        return va;
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

        String exprType = ((ExpressionStmt)exprStmt).getType();
        System.out.println("var def lhs:" + type);
        System.out.println("var def rhs:" + exprType);
        TypeInfoVariableDef typeInfo = TypeResolver.resolveVariableDefType(type, exprType);

        VariableDefinition vd = new VariableDefinition(typeInfo, varId, exprStmt);

        return vd;
    }

    @Override
    public Statement visitNativeFuncDeclStmt(NativeFuncDeclStmtContext ctx) {
        String funcId = ctx.IDENTIFIER().getText();
        ArrayList<FuncArg> args = new ArrayList<>();
        TypeFuncCall return_type = new TypeFuncCall();
        return_type.result_type = ctx.cTypeName().getText();
        NativeFunctionDecl nfd = null;

        for (int i = 0; i < ctx.cArgument().size(); i++) {
            String argId = "";
            if (ctx.cArgument(i).IDENTIFIER() != null) {
                ctx.cArgument(i).IDENTIFIER().getText();
            }
            FuncArg arg = new FuncArg(
                ctx.cArgument(i).cTypeName().getText(), 
                argId,
                false
            );
            args.add(arg);
        }
        
        // just add decleration part, it is enough
        nfd = new NativeFunctionDecl(funcId, args, return_type);
        
        Program.nativeFuncDeclSymbols.put(funcId, nfd);

        return nfd;
    }

    
}
