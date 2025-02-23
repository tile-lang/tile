package tile;

import gen.antlr.tile.tileParser.ProgramContext;
import gen.antlr.tile.tileParserBaseVisitor;
import tile.ast.base.*;
import tile.ast.stmt.VariableDefinition;
import tile.sym.TasmSymbolGenerator;

public class AntlrToProgram extends tileParserBaseVisitor<Program>{

    @Override
    public Program visitProgram(ProgramContext ctx) {
        Program program = new Program();

        AntlrToStatement statementVisitor = new AntlrToStatement();
        for (int i = 0; i < ctx.globalStatements().globalStatement().size(); i++) {
            Statement stmt = statementVisitor.visit(ctx.globalStatements().globalStatement(i));
            if (stmt instanceof VariableDefinition) {
                
                String globalTasmVarId = TasmSymbolGenerator.tasmGenGlobalVariableName(((VariableDefinition)stmt).getVarId());

                Program.globalVariableSymbols.put(globalTasmVarId, ((VariableDefinition)stmt));
            }
            program.addStatement(stmt);
        }

        return program;
    }
    
}
