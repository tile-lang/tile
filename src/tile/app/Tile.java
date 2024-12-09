package tile.app;

import java.io.IOException;
import java.util.List;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;

import gen.antlr.tile.tileLexer;
import gen.antlr.tile.tileParser;
import tile.AntlrToProgram;
import tile.Program;
import tile.ast.stmt.FunctionDefinition;

public class Tile {
    public static void main(String args[]) {
        tileParser parser = createTileParser("examples/test.tile");
        ParseTree ast = parser.program();
        AntlrToProgram programVisitor = new AntlrToProgram();
        Program program = programVisitor.visit(ast);

        program.generate();

        System.out.println(FunctionDefinition.funcDefSymbols.keySet());
        for (FunctionDefinition value : FunctionDefinition.funcDefSymbols.values()) {
            System.out.println(value.variableSymbols.keySet());
        }
    }

    private static tileParser createTileParser(String filePath) {
        tileParser parser = null;
        
        try {
            CharStream input = CharStreams.fromFileName(filePath);
            tileLexer lexer = new tileLexer(input);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            
            // // Force the token stream to fetch all tokens
            // tokens.fill();
            // // Print tokens and their types
            // for (Token token : tokens.getTokens()) {
            //     System.out.println(token.getText() + " -> " + token.getType());
            // }

            parser = new tileParser(tokens);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return parser;
    }
}
