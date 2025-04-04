package tile;

import java.util.HashMap;
import java.util.Map;

import gen.antlr.tile.tileParser.FuncDefStmtContext;
import gen.antlr.tile.tileParser.NativeFuncDeclStmtContext;
import gen.antlr.tile.tileParser.PrimaryExpressionContext;
import gen.antlr.tile.tileParserBaseVisitor;

public class PrePassStatement extends tileParserBaseVisitor<Void> {

    private Program program;
    public static Map<String, Integer> globalDataTableIndices = new HashMap<>();
    public static Map<String, Integer> globalDataTable = new HashMap<>();
    private static int globalDataTableIndicesCounter = 0;
    private static int globalDataTableCounter = 0;

    public static int dataTableIndicesGetOrAdd(String str) {
        return globalDataTableIndices.computeIfAbsent(str, k -> globalDataTableIndicesCounter++);
    }

    public static boolean dataTableIsNew(String str) {
        boolean isNew = !globalDataTable.containsKey(str);
        globalDataTable.computeIfAbsent(str, k -> globalDataTableCounter++);
        return isNew;
    }

    public PrePassStatement(Program program) {
        this.program = program;
    }

    @Override
    public Void visitFuncDefStmt(FuncDefStmtContext ctx) {
        // TODO Auto-generated method stub
        return super.visitFuncDefStmt(ctx);
    }

    @Override
    public Void visitNativeFuncDeclStmt(NativeFuncDeclStmtContext ctx) {
        // TODO Auto-generated method stub
        return super.visitNativeFuncDeclStmt(ctx);
    }

    @Override
    public Void visitPrimaryExpression(PrimaryExpressionContext ctx) {
        
        if (ctx.STRING_LITERAL() != null) {
            String strLiteral = ctx.STRING_LITERAL().getText();
            
            String generatedTasmData = "";
            generatedTasmData = generateTasmData(generatedTasmData, strLiteral);
            program.pushBackGeneratedCode(generatedTasmData);
        }
        
        return super.visitPrimaryExpression(ctx);
    }

    public String generateTasmData(String generatedCodeData, String strLiteral) {
        //TODO: preprocess the string
        
        boolean added = dataTableIsNew(strLiteral);
        System.out.println("added: " + added);
        if (added) {
            generatedCodeData += "@data " + strLiteral + "\n";
            generatedCodeData += "@data " + "0x00" + Integer.toHexString(strLiteral.length() - 2) + "\n";
        }

        return generatedCodeData;
    }
    
}
