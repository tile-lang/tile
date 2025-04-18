package tile;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

import gen.antlr.tile.tileParser.BlockStmtContext;
import gen.antlr.tile.tileParser.ExpressionStmtContext;
import gen.antlr.tile.tileParser.ForStmtContext;
import gen.antlr.tile.tileParser.ForUpdateContext;
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
import gen.antlr.tile.tileParser.VariableStmtContext;
import gen.antlr.tile.tileParser.WhileStmtContext;
import gen.antlr.tile.tileParser.TypeDefinitionContext;
import gen.antlr.tile.tileParser.StructDefinitionContext;
import gen.antlr.tile.tileParser.FieldDefinitionContext;
import gen.antlr.tile.tileParser.ForInitialContext;
import gen.antlr.tile.tileParser.TypeUnionContext;
import gen.antlr.tile.tileParserBaseVisitor;
import tile.app.Log;
import tile.ast.base.*;
import tile.ast.stmt.*;
import tile.ast.stmt.FunctionDefinition.FuncArg;
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
                    Log.error(line + ": variable " + "'" + varId + "' is already defined in the same scope!");
                }
                blockStmt.variableSymbols.put(tasmVarSym, vd);
            }
        } else if (parent instanceof ForStmtContext) {
            if (!Program.parentStack.isEmpty() && Program.parentStack.peek() instanceof ForStmt) {
                ForStmt parentForStmt = (ForStmt) Program.parentStack.peek();
                Statement init = parentForStmt.getInit();

                int blockId = blockStmt.getBlockId();
                String varId = null;
                String type = null;
                VariableStmtContext vctx = ((ForStmtContext)parent).forInitial().variableStmt();
                if (vctx != null) {
                    if (vctx.variableDefinition() != null) {
                        varId = vctx.variableDefinition().IDENTIFIER().getText();
                        type = vctx.variableDefinition().typeName().getText();
                    }
                }
                variableTasmId = func.getTasmVarIdx();
                String tasmVarSym = TasmSymbolGenerator.tasmGenVariableName(blockId, varId);
                Statement exprStmt = visit(vctx.variableDefinition().expressionStmt());
                String exprType = ((ExpressionStmt)exprStmt).getType();
    
                if (blockStmt.variableSymbols.containsKey(tasmVarSym)) {
                    int line = ((FuncDefStmtContext)parentFunc).IDENTIFIER().getSymbol().getLine();
                    Log.error(line + ": variable " + "'" + varId + "' is already defined in the same scope!");
                }

                if (init instanceof VariableDefinition) {
                    TypeInfoVariableDef typeInfo = TypeResolver.resolveVariableDefType(type, exprType);
                    VariableDefinition vd = new VariableDefinition(typeInfo, varId, exprStmt);
                    vd.setTasmIdx(variableTasmId);
                    init = vd;
                    blockStmt.variableSymbols.put(tasmVarSym, vd);
                    Log.debug("forstmt:tasmVarSym: " + tasmVarSym);
                } else if (init instanceof VariableDecleration) {
                    TypeInfoVariableDef typeInfo = TypeResolver.resolveVariableDefType(type, exprType);
                    VariableDecleration vd = new VariableDecleration(typeInfo.var_type, varId);
                    vd.setTasmIdx(variableTasmId);
                    init = vd;
                    blockStmt.variableSymbols.put(tasmVarSym, vd);
                }
                parentForStmt.setInit(init);
            }
        }

        
        Log.debug("HEREME: " + Program.blockStack.size());

        if (ctx.localStatements() == null) {
            if (Program.parentStack.isEmpty() || !(Program.parentStack.peek() instanceof ForStmt)) {
                Program.blockStack.remove(Program.blockStack.size() - 1);
            }
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
                    Log.error(line + ": variable " + "'" + varId + "' is already defined in the same scope!");
                }

                blockStmt.variableSymbols.put(tasmVarSym, ((VariableDefinition)stmt));
            } else if (stmt instanceof VariableDecleration) {
                variableTasmId = func.getTasmVarIdx();
                ((VariableDecleration)stmt).setTasmIdx(variableTasmId);

                String varId = ((VariableDecleration)stmt).getVarId();
                int blockId = blockStmt.getBlockId();
                String tasmVarSym = TasmSymbolGenerator.tasmGenVariableName(blockId, varId);

                if (blockStmt.variableSymbols.containsKey(tasmVarSym)) {
                    int line = ((FuncDefStmtContext)parentFunc).IDENTIFIER().getSymbol().getLine();
                    Log.error(line + ": variable " + "'" + varId + "' is already defined in the same scope!");
                }

                blockStmt.variableSymbols.put(tasmVarSym, ((VariableDecleration)stmt));
            }
            blockStmt.addStatement(stmt);
        }
        

        if (Program.parentStack.isEmpty() || !(Program.parentStack.peek() instanceof ForStmt)) {
            Program.blockStack.remove(Program.blockStack.size() - 1);
        }

        return blockStmt;
    }

    @Override
    public Statement visitExpressionStmt(ExpressionStmtContext ctx) {
        // We need to eliminate code generation for ExpressionStmtContext whose parents are NOT ReturnStmtContext.
        // this will eliminate code lines like: "5;" or "3 + 8 * 2;" to generate 'push' and 'binop(mult, add etc.)' instructions.
        boolean generate = false;
        if ((ctx.getParent() instanceof ReturnStmtContext) || (ctx.getChild(0).getChild(0) instanceof FuncCallExpressionContext) || ctx.getParent() instanceof VariableDefinitionContext || ctx.getParent() instanceof VariableAssignmentContext || ctx.getParent() instanceof ForStmtContext) {
            generate = true;
        } else {
            if (ctx.expression().unaryExpression() != null) {
                if (ctx.expression().unaryExpression().incDecOperator() != null) {
                    generate = true;
                }
            }
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
        Statement init = visit(ctx.forInitial());
        
        // Create the ForStmt but don't return it yet
        ForStmt forStmt = new ForStmt(init, null, null, null);
        Program.parentStack.push(forStmt);
        // Visit the block, which can now modify the parent ForStmt
        Statement body = visit(ctx.blockStmt());
        Statement condition = visit(ctx.expressionStmt());

        if (!(((ExpressionStmt)condition).getType().equals("bool"))) {
            int line = ctx.KW_FOR().getSymbol().getLine();
            System.err.println("WARNING:" + line + ": for condition expression type should be a bool type!");
        }

        AntlrToExpression exprVisitor = new AntlrToExpression();
        Expression update = null;
        if (ctx.forUpdate() != null) {
            update = exprVisitor.visit(ctx.forUpdate());
        }

        forStmt.setBody(body);
        forStmt.setCondition(condition);
        forStmt.setUpdate(update);
        Program.parentStack.pop();

        Program.blockStack.remove(Program.blockStack.size() - 1);
        return forStmt;
    }

    @Override
    public Statement visitForInitial(ForInitialContext ctx) {
        Statement result = null;
        if (ctx.expressionStmt() != null) {
            result = visit(ctx.expressionStmt());
        }
        else if (ctx.variableStmt() != null) {
            result = visit(ctx.variableStmt());
        }

        return result;
    }

    // @Override
    // public Statement visitTypeDefinition(TypeDefinitionContext ctx) {
    //     String name = ctx.IDENTIFIER().getText();

    //     if (ctx.structDefinition() != null) {
    //         TypeDefinition def = new TypeDefinition(name, TypeDefinition.Kind.STRUCT);
    //         List<String> fields = new ArrayList<>();

    //         for (var fieldCtx : ctx.structDefinition().fieldDefinition()) {
    //             String fieldName = fieldCtx.IDENTIFIER().getText();
    //             String fieldType = fieldCtx.typeName().getText();
    //             fields.add(new String(fieldName, fieldType));
    //         }

    //         def.fields = fields;
    //         TypeResolver.userTypeDefs.put(name, def);

    //     } else if (ctx.typeUnion() != null) {
    //         TypeDefinition def = new TypeDefinition(name, TypeDefinition.Kind.UNION);

    //         List<String> variants = ctx.typeUnion().IDENTIFIER()
    //                 .stream()
    //                 .map(ParseTree::getText)
    //                 .toList();

    //         def.variants = variants;
    //         TypeResolver.userTypeDefs.put(name, def);
    //     }

    //     return new ExpressionStmt(null, false);
    //     return super.visitTypeDefinition(ctx);
    // }

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
        visit(ctx.blockStmt());

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
            Log.error(line + ": " + "return statement cannot be used outside a function definiton!");
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
            Log.warning(line + ": while condition expression type should be a bool type!");
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
            Log.error(line + ": variable " + "'" + varId + "' is not defined before assignment!");
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
                    Log.error(line + ": Array index specifier setter must be 'int' type!");
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
        // int a;
        // Cat b;

        String type = ctx.typeName().getText();
        String varId = ctx.IDENTIFIER().getText();

        VariableDecleration v_dec = new VariableDecleration(type, varId);

        return v_dec;
    }

    @Override
    public Statement visitVariableDefinition(VariableDefinitionContext ctx) {
        String type = ctx.typeName().getText();
        String varId = ctx.IDENTIFIER().getText();
        Statement exprStmt = visit(ctx.expressionStmt());

        String exprType = ((ExpressionStmt)exprStmt).getType();
        Log.debug("var def lhs:" + type);
        Log.debug("var def rhs:" + exprType);
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
