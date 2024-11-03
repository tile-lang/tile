package tile;

import gen.antlr.tile.tileParser.ProgramContext;
import gen.antlr.tile.tileParserBaseVisitor;
import tile.ast.base.*;

public class AntlrToProgram extends tileParserBaseVisitor<Program>{

    @Override
    public Program visitProgram(ProgramContext ctx) {
        Program program = new Program();

        AntlrToStatement statementVisitor = new AntlrToStatement();
        for (int i = 0; i < ctx.statements().statement().size(); i++) {
            Statement stmt = statementVisitor.visit(ctx.statements().statement(i));
            program.addStatement(stmt);
        }

        return program;
    }
    
}
