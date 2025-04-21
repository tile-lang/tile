package tile;

import gen.antlr.tile.tileParser.ProgramContext;
import gen.antlr.tile.tileParserBaseVisitor;
import tile.app.Log;
import tile.ast.base.*;
import tile.ast.stmt.Variable;
import tile.sym.TasmSymbolGenerator;

public class AntlrToProgram extends tileParserBaseVisitor<Program>{

    @Override
    public Program visitProgram(ProgramContext ctx) {
        Program program = new Program();
        if (ctx.globalStatements() == null) return program;

        AntlrToStatement statementVisitor = new AntlrToStatement();
        for (int i = 0; i < ctx.globalStatements().globalStatement().size(); i++) {
            Statement stmt = statementVisitor.visit(ctx.globalStatements().globalStatement(i));
            if (stmt instanceof Variable) {
                
                String globalTasmVarId = TasmSymbolGenerator.tasmGenGlobalVariableName(((Variable)stmt).getVarId());
                Log.info("globalTasmVarId:" + globalTasmVarId);

                Program.globalVariableSymbols.put(globalTasmVarId, ((Variable)stmt));
            }
            program.addStatement(stmt);
        }

        return program;
    }
    
}
