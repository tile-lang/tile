package tile;

import gen.antlr.tile.tileParser.AdditiveExpressionContext;
import gen.antlr.tile.tileParser.AndExpressionContext;
import gen.antlr.tile.tileParser.ArrayIndexAccessorContext;
import gen.antlr.tile.tileParser.ArraySizedInitializerContext;
import gen.antlr.tile.tileParser.ArrayValuedInitializerContext;
import gen.antlr.tile.tileParser.AssignmentExpressionContext;
import gen.antlr.tile.tileParser.CastExpressionContext;
import gen.antlr.tile.tileParser.ConditionalExpressionContext;
import gen.antlr.tile.tileParser.EqualityExpressionContext;
import gen.antlr.tile.tileParser.ExclusiveOrExpressionContext;
import gen.antlr.tile.tileParser.ExpressionContext;
import gen.antlr.tile.tileParser.ExpressionStmtContext;
import gen.antlr.tile.tileParser.ForUpdateContext;
import gen.antlr.tile.tileParser.FuncCallExpressionContext;
import gen.antlr.tile.tileParser.InclusiveOrExpressionContext;
import gen.antlr.tile.tileParser.LogicalAndExpressionContext;
import gen.antlr.tile.tileParser.LogicalOrExpressionContext;
import gen.antlr.tile.tileParser.MultiplicativeExpressionContext;
import gen.antlr.tile.tileParser.ObjectAccessorContext;
import gen.antlr.tile.tileParser.ObjectLiteralExpressionContext;
import gen.antlr.tile.tileParser.PrimaryExpressionContext;
import gen.antlr.tile.tileParser.RelationalExpressionContext;
import gen.antlr.tile.tileParser.ShiftExpressionContext;
import gen.antlr.tile.tileParser.UnaryExpressionContext;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.antlr.v4.runtime.ParserRuleContext;

import gen.antlr.tile.tileParserBaseVisitor;
import tile.app.Log;
import tile.ast.base.Expression;
import tile.ast.expr.AdditativeExpression;
import tile.ast.expr.ArrayIndexAccessor;
import tile.ast.expr.ArrayInitializer;
import tile.ast.expr.CastExpression;
import tile.ast.expr.EqualityExpression;
import tile.ast.expr.FuncCallExpression;
import tile.ast.expr.LogicalExpression;
import tile.ast.expr.MultiplicativeExpression;
import tile.ast.expr.ObjectAccessor;
import tile.ast.expr.ObjectLiteral;
import tile.ast.expr.PrimaryExpression;
import tile.ast.expr.RelationalExpression;
import tile.ast.expr.ShiftExpression;
import tile.ast.expr.UnaryExpression;
import tile.ast.stmt.TypeDefinition;
import tile.ast.types.TypeResolver;
import tile.ast.types.TypeResolver.TypeFuncCall;
import tile.ast.types.TypeResolver.TypeInfoArray;
import tile.ast.types.TypeResolver.TypeInfoBinop;
import tile.ast.types.TypeResolver.TypeInfoBinopBool;
import tile.ast.types.TypeResolver.TypeInfoBinopInt;
import tile.ast.types.TypeResolver.TypeInfoCast;
import tile.ast.types.TypeResolver.TypeInfoLogicalBinop;
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
            if (((UnaryExpressionContext)parent).unaryOperator() != null) {
                unaryOp = ((UnaryExpressionContext)parent).unaryOperator().getText();
            } else if (((UnaryExpressionContext)parent).incDecOperator() != null) {
                unaryOp = ((UnaryExpressionContext)parent).incDecOperator().getText();
            }
        }

        if (count == 1) { 
            if (ctx.INT_LITERAL() != null) {
                String intLiteral = ctx.INT_LITERAL().getText();
                expr = new PrimaryExpression(unaryOp, intLiteral, "int", false, 0, 0);
            }
            else if (ctx.FLOAT_LITERAL() != null) {
                String floatLiteral = ctx.FLOAT_LITERAL().getText();
                expr = new PrimaryExpression(unaryOp, floatLiteral, "float", false, 0, 0);
            }
            else if (ctx.CHAR_LITERAL() != null) {
                String chrLiteral = ctx.CHAR_LITERAL().getText();
                expr = new PrimaryExpression(unaryOp, chrLiteral, "char", false, 0, 0);
            }
            else if (ctx.BOOL_LITERAL() != null) {
                String boolLiteral = ctx.BOOL_LITERAL().getText();
                Log.debug("BOOL_LITERAL: " + boolLiteral);
                if (boolLiteral.equals("true")) {
                    expr = new PrimaryExpression(unaryOp, "1", "bool", false, 0, 0);
                }
                else if (boolLiteral.equals("false")) {
                    expr = new PrimaryExpression(unaryOp, "0", "bool", false, 0, 0);
                }
            }
            else if (ctx.STRING_LITERAL() != null) {
                String strLiteral = ctx.STRING_LITERAL().getText();
                int dataTasmIdx = PrePassStatement.dataTableIndicesGetOrAdd(strLiteral);
                Log.debug("STR_LITERAL: " + strLiteral + " : " + dataTasmIdx);
                
                expr = new PrimaryExpression(unaryOp, strLiteral, "string", false, 0, dataTasmIdx);
            }
            else if (ctx.IDENTIFIER() != null) {
                String identifier = ctx.IDENTIFIER().getText();
                StringBuilder varType = new StringBuilder();
                int tasmIdx = -1;
                AtomicBoolean isGlobal = new AtomicBoolean(false);
                try {
                    tasmIdx = TasmSymbolGenerator.identifierScopeFind(identifier, varType, isGlobal);
                } catch (Exception e) {
                    int line = ctx.IDENTIFIER().getSymbol().getLine();
                    int col = ctx.IDENTIFIER().getSymbol().getCharPositionInLine();
                    Log.error(line + ":" + col + ": variable " + "'" + identifier + "' is not defined before use!");
                }

                expr = new PrimaryExpression(unaryOp, identifier, varType.toString(), true, tasmIdx, 0);
                if (isGlobal.get() == true) {
                    ((PrimaryExpression)expr).setAsGlobal();
                }

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
            Log.error("TODO not reachable!");
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
            } else if (ctx.arrayIndexAccessor() != null) {
                return visit(ctx.arrayIndexAccessor());
            } else if (ctx.objectAccessor() != null) {
                return visit(ctx.objectAccessor());
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
        } else if (ctx.arrayIndexAccessor() != null) {
            expr = visit(ctx.arrayIndexAccessor());
        } else if (ctx.objectAccessor() != null) {
            expr = visit(ctx.objectAccessor());
        }
        expr_type = expr.getType();

        TypeInfoCast type = TypeResolver.resolveCastType(expr_type, cast_type);
        // System.out.println("debug cast : " + type.cast_type);
        // System.out.println("debug expr : " + type.expr_type);
        // System.out.println("debug result : " + type.result_type);
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
        // If there's no operator, directly visit the single child (relationalExpression).
        if (ctx.relationalExpression().size() == 1) {
            return visit(ctx.relationalExpression(0));
        }

        // Otherwise, process the operator and operands.
        Expression left = visit(ctx.relationalExpression(0)); // The first operand.
        for (int i = 1; i < ctx.relationalExpression().size(); i++) {
            // Get the operator (+ or -).
            String operator = ctx.getChild((i * 2) - 1).getText(); // Operators are at odd indices.

            // Visit the right operand.
            Expression right = visit(ctx.relationalExpression(i));

            String lhs_type = left.getType();
            String rhs_type = right.getType();;
            TypeInfoBinopBool type = TypeResolver.resolveBinopBooleanTypeEquality(lhs_type, rhs_type);
            left = new EqualityExpression(left, operator, right, type);
        }
        return left;
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
        int col = ctx.IDENTIFIER().getSymbol().getCharPositionInLine();

        boolean is_native = false;

        String tasmFuncSym = TasmSymbolGenerator.tasmGenFunctionName(funcId);
        //FIXME:
        try {
            type.result_type = Program.funcDefSymbols.get(tasmFuncSym).getReturnType();
        } catch (NullPointerException ne) {
            
            try {
                type.result_type = Program.nativeFuncDeclSymbols.get(funcId).getReturnType();
                is_native = true;
            } catch (Exception e) {
                Log.error(line + ":" + col + ": function " + funcId + " is not defined before called.");
                return null; // FIXME: find a better way and consider func overloading as well for the future!
            }
        }

        int arg_size = -1;
        if (is_native == false) {
            arg_size = Program.funcDefSymbols.get(tasmFuncSym).getArgs().size();
        } else {
            arg_size = Program.nativeFuncDeclSymbols.get(funcId).getArgs().size();
        }

        // TODO: check nativeness
        if (ctx.primaryExpression() == null && ctx.funcCallExpression() == null) {
            if (arg_size != ctx.expression().size()) {
                Log.error(line + ":" + col + ": function call" + funcId + " doesn't match by argument count, expected " + arg_size + " but got " +ctx.expression().size());
                return null;
            }
    
            for (int i = 0; i < ctx.expression().size(); i++) {
                Expression expr = visit(ctx.expression(i));
                arg_exprs.add(expr);
            }
        } else {
            if (arg_size != ctx.expression().size() + 1) {
                Log.error(line + ":" + col + ": function call" + funcId + " doesn't match by argument count, expected " + arg_size + " but got " + (ctx.expression().size() + 1));
                return null;
            }
            if (ctx.primaryExpression() != null) {
                Expression prim_expr = visit(ctx.primaryExpression());
                arg_exprs.add(prim_expr);
            } else if (ctx.funcCallExpression() != null) {
                Expression funccall_expr = visit(ctx.funcCallExpression());
                arg_exprs.add(funccall_expr);
            }
            for (int i = 0; i < ctx.expression().size(); i++) {
                Expression expr = visit(ctx.expression(i));
                arg_exprs.add(expr);
            }
        }

        FuncCallExpression fce = new FuncCallExpression(funcId, arg_exprs, type, is_native);

        return fce;
    }

    @Override
    public Expression visitInclusiveOrExpression(InclusiveOrExpressionContext ctx) {
        // TODO Auto-generated method stub
        return super.visitInclusiveOrExpression(ctx);
    }

    @Override
    public Expression visitLogicalAndExpression(LogicalAndExpressionContext ctx) {
        if (ctx.equalityExpression().size() == 1) {
            return visit(ctx.equalityExpression(0));
        }

        // Otherwise, process the operator and operands.
        Expression left = visit(ctx.equalityExpression(0)); // The first operand.
        for (int i = 1; i < ctx.equalityExpression().size(); i++) {
            // Get the operator (* or /).
            String operator = ctx.getChild((i * 2) - 1).getText(); // Operators are at odd indices.

            // Visit the right operand.
            Expression right = visit(ctx.equalityExpression(i));

            String lhs_type = left.getType();
            String rhs_type = right.getType();

            if (!lhs_type.equals("bool") || !rhs_type.equals("bool")) {
                int line = ctx.stop.getLine();
                int col = ctx.stop.getCharPositionInLine();
                Log.warning(line + ":" + col + ": logical expressions type should be a bool type!");
            }
            TypeInfoLogicalBinop type = TypeResolver.resolveBinopLogicalType(lhs_type, rhs_type);
            left = new LogicalExpression(left, operator, right, type);
        }
        return left;
    }

    @Override
    public Expression visitLogicalOrExpression(LogicalOrExpressionContext ctx) {
        if (ctx.logicalAndExpression().size() == 1) {
            return visit(ctx.logicalAndExpression(0));
        }

        // Otherwise, process the operator and operands.
        Expression left = visit(ctx.logicalAndExpression(0)); // The first operand.
        for (int i = 1; i < ctx.logicalAndExpression().size(); i++) {
            // Get the operator (* or /).
            String operator = ctx.getChild((i * 2) - 1).getText(); // Operators are at odd indices.

            // Visit the right operand.
            Expression right = visit(ctx.logicalAndExpression(i));

            String lhs_type = left.getType();
            String rhs_type = right.getType();

            if (!lhs_type.equals("bool") || !rhs_type.equals("bool")) {
                int line = ctx.stop.getLine();
                int col = ctx.stop.getCharPositionInLine();
                Log.warning(line + ":" + col + ": logical expressions type should be a bool type!");
            }
            TypeInfoLogicalBinop type = TypeResolver.resolveBinopLogicalType(lhs_type, rhs_type);
            left = new LogicalExpression(left, operator, right, type);
        }
        return left;
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
        if (ctx.inclusiveOrExpression().size() == 1) {
            return visit(ctx.inclusiveOrExpression(0));
        }

        // Otherwise, process the operator and operands.
        Expression left = visit(ctx.inclusiveOrExpression(0)); // The first operand.
        for (int i = 1; i < ctx.inclusiveOrExpression().size(); i++) {
            // Get the operator (* or /).
            String operator = ctx.getChild((i * 2) - 1).getText(); // Operators are at odd indices.

            // Visit the right operand.
            Expression right = visit(ctx.inclusiveOrExpression(i));

            String lhs_type = left.getType();
            String rhs_type = right.getType();
            TypeInfoBinopBool type = TypeResolver.resolveBinopBooleanType(lhs_type, rhs_type);
            left = new RelationalExpression(left, operator, right, type);
        }
        return left;
    }

    @Override
    public Expression visitShiftExpression(ShiftExpressionContext ctx) {
        List<AdditiveExpressionContext> exprs = ctx.additiveExpression();

        if (exprs.size() == 2) {
            Expression left = visit(exprs.get(0));
            Expression right = visit(exprs.get(1));
            String operator = ctx.getChild(1).getText();
            return new ShiftExpression(left, operator, right, TypeResolver.resolveBinopShiftType(left.getType(), right.getType()));
        } else if (exprs.size() == 1) {
            return visit(exprs.get(0));
        }

        throw new RuntimeException("ShiftExpressionContext has no expressions.");
    }


    @Override
    public Expression visitUnaryExpression(UnaryExpressionContext ctx) {
        Expression expr = null;
        if (ctx.primaryExpression() != null) {
            expr = visit(ctx.primaryExpression());
            String type = expr.getType();
            if (ctx.unaryOperator() != null) {
                String unaryOp = ctx.unaryOperator().getText();

                if (unaryOp.equals("+") || unaryOp.equals("-")) {
                    if (!(type.equals("int") || type.equals("float"))) {
                        Log.error("'+' and '-' prefixes cannot go before any non-numeric type!");
                    }
                } else if (unaryOp.equals("!")) {
                    if (!type.equals("bool")) {
                        Log.error("'" + unaryOp + "' operator cannot go before this type: " + type);
                    }
                }
            }
            else if (ctx.incDecOperator() != null) {
                String incDecOp = ctx.incDecOperator().getText();
                if (ctx.primaryExpression().IDENTIFIER() != null) {
                    if (!(type.equals("int") || type.equals("float"))) {
                        Log.error("'" + incDecOp + "' operator cannot go before any non-numeric type!");
                    }
                } else {
                    Log.error("'" + incDecOp + " operator had to be used with an identifier!");
                }
            }
        } else {
            if (ctx.unaryOperator() != null) {
                String unaryOp = ctx.unaryOperator().getText();
                expr = visit(ctx.children.get(1));
                return new UnaryExpression(unaryOp, expr);
            }
        }

        return expr;
    }

    @Override
    public Expression visitArraySizedInitializer(ArraySizedInitializerContext ctx) {
        String type = ctx.primaryTypeName().getText();
        int dim = ctx.arraySizeSpecifier().size();
        List<Expression> arrSizes = new ArrayList<>();
        for (int i = 0; i < dim; i++) {
            Expression expr = visit(ctx.arraySizeSpecifier(i).expression());
            String sizeExprType = expr.getType();
            if (!sizeExprType.equals("int")) {
                int line = ctx.primaryTypeName().getStop().getLine();
                int col = ctx.primaryTypeName().getStop().getCharPositionInLine();
                Log.error(line + ":" + col + ": Array sized initializer must be an 'int' type!");
            }
            arrSizes.add(expr);
        }
        TypeInfoArray typeInfo = TypeResolver.resolveArrayInitializerType(type, dim);

        ArrayInitializer ai = new ArrayInitializer(typeInfo, arrSizes);
        return ai;
    }

    @Override
    public Expression visitArrayValuedInitializer(ArrayValuedInitializerContext ctx) {
        // TODO Auto-generated method stub
        return super.visitArrayValuedInitializer(ctx);
    }

    @Override
    public Expression visitArrayIndexAccessor(ArrayIndexAccessorContext ctx) {
        String identifier = ctx.IDENTIFIER().getText();
        StringBuilder varType = new StringBuilder();
        int tasmIdx = -1;
        AtomicBoolean isGlobal = new AtomicBoolean(false);
        try {
            tasmIdx = TasmSymbolGenerator.identifierScopeFind(identifier, varType, isGlobal);
            // TODO: use isGlobal!!!
        } catch (Exception e) {
            int line = ctx.IDENTIFIER().getSymbol().getLine();
            int col = ctx.IDENTIFIER().getSymbol().getCharPositionInLine();
            Log.error(line + ":" + col + ": variable " + "'" + identifier + "' is not defined before use!");
        }

        ctx.arrayIndexSpecifier().size();

        Log.debug("varType: " + varType.toString());

        List<Expression> exprs = new ArrayList<>();
        for (int i = 0; i < ctx.arrayIndexSpecifier().size(); i++) {
            Expression expr = visit(ctx.arrayIndexSpecifier(i).expression());
            if (!TypeResolver.isIntType(expr.getType())) {
                int line = ctx.IDENTIFIER().getSymbol().getLine();
                int col = ctx.IDENTIFIER().getSymbol().getCharPositionInLine();
                Log.error(line + ":" + col + ": Array index specifier must be 'int' type!");
            }
            exprs.add(expr);
        }

        TypeInfoArray typeInfo = new TypeInfoArray();
        int reducedDim = exprs.size();
        typeInfo = TypeResolver.resolveArrayIndexAccessor(varType.toString(), reducedDim);

        ArrayIndexAccessor aia = new ArrayIndexAccessor(typeInfo, tasmIdx, exprs);
        if (isGlobal.get() == true) {
            aia.setAsGlobal();
        }

        return aia;
    }

    @Override
    public Expression visitForUpdate(ForUpdateContext ctx) {
        Expression result = null;
        if (ctx.unaryExpression() != null) {
            Log.debug("ctx.unaryExpression");
            result = visit(ctx.unaryExpression());
        } else if (ctx.forUpdateAssingment() != null) {
            Log.debug("ctx.forUpdateAssingment");
            result = visit(ctx.forUpdateAssingment());
        } else if (ctx.funcCallExpression() != null) {
            Log.debug("ctx.funcCallExpression");
            result = visit(ctx.funcCallExpression());
        }
        Log.debug("visitForUpdate result " + result);

        return result;
    }

    @Override
    public Expression visitObjectLiteralExpression(ObjectLiteralExpressionContext ctx) {
        String type = "";
        if (ctx.IDENTIFIER() != null) {
            // Implemented
            /*
                a: Animal = Animal {
                    .name = "asd",
                    .age = 10
                }; 
            */
            type = ctx.IDENTIFIER().getText();
            TypeDefinition td = TypeResolver.userTypeDefs.get(type);
            if (td == null) {
                int line = ctx.IDENTIFIER().getSymbol().getLine();
                int col = ctx.IDENTIFIER().getSymbol().getCharPositionInLine();
                Log.error(line + ":" + col + ": literal type " + type + " cannot be resolved!");
            } else {
                // traverse {.identifier} and typedefinition fields to see if they matched
                if (ctx.objectLiteralFieldAssignment() != null) {
                    int[] assignedFields = new int[td.getFields().size()];
                    for (int i = 0; i < ctx.objectLiteralFieldAssignment().size(); i++) {
                        String objLitFieldId = ctx.objectLiteralFieldAssignment(i).IDENTIFIER().getText();

                        if (td.getFields().get(objLitFieldId) == null) {
                            int line = ctx.objectLiteralFieldAssignment(i).IDENTIFIER().getSymbol().getLine();
                            int col = ctx.objectLiteralFieldAssignment(i).IDENTIFIER().getSymbol().getCharPositionInLine();
                            Log.error(line + ":" + col + ": " + type + " doesn't have a field " + objLitFieldId + "!");
                        }
                    }

                    ObjectLiteral objLit = new ObjectLiteral(td, type, assignedFields);
                    return objLit;
                }
            }

        } else {
            // TODO: Not implemented inferring type
            /*
                a: Animal = {0}; 
                a: Animal = {
                    .name = "asd",
                    .age = 10
                }; 
            */
            int line = ctx.PUNC_LEFTBRACE().getSymbol().getLine();
            int col = ctx.PUNC_LEFTBRACE().getSymbol().getCharPositionInLine();
            Log.error(line + ":" + col + ": for custom literal type, type inferring is not implemented yet!");
        }


        
        return super.visitObjectLiteralExpression(ctx);
    }

    @Override
    public Expression visitObjectAccessor(ObjectAccessorContext ctx) {
        String identifier = ctx.IDENTIFIER(0).getText();
        StringBuilder varType = new StringBuilder();
        int tasmIdx = -1;
        AtomicBoolean isGlobal = new AtomicBoolean(false);
        try {
            tasmIdx = TasmSymbolGenerator.identifierScopeFind(identifier, varType, isGlobal);
            // TODO: use isGlobal!!!
        } catch (Exception e) {
            int line = ctx.IDENTIFIER(0).getSymbol().getLine();
            int col = ctx.IDENTIFIER(0).getSymbol().getCharPositionInLine();
            Log.error(line + ":" + col + ": variable " + "'" + identifier + "' is not defined before use!");
        }

        String type = varType.toString();
        TypeDefinition td = TypeResolver.userTypeDefs.get(type);


        if (ctx.objectAccessor() == null) {
            String fieldId = ctx.IDENTIFIER(1).getText();
            if (td != null) {
                td.getFields().get(fieldId);
                if (td.getFields().get(fieldId) == null) {
                    int line = ctx.IDENTIFIER(1).getSymbol().getLine();
                    int col = ctx.IDENTIFIER(1).getSymbol().getCharPositionInLine();
                    Log.error(line + ":" + col + ": '" + identifier + ": " + type + "'' doesn't have a field " + fieldId + "!");
                }
            }

            ObjectAccessor oa = new ObjectAccessor(fieldId, td, tasmIdx, null);
            if (isGlobal.get() == true) {
                oa.setAsGlobal();
            }

            return oa;
        } else {
            
            ObjectAccessor accessor = (ObjectAccessor)visit(ctx.objectAccessor());

            ObjectAccessor oa = new ObjectAccessor(null, td, tasmIdx, accessor);
            if (isGlobal.get() == true) {
                oa.setAsGlobal();
            }

            return oa;
        }        
    }

}
