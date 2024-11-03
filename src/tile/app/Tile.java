package tile.app;

import java.io.IOException;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import gen.antlr.tile.tileLexer;
import gen.antlr.tile.tileParser;
import tile.AntlrToProgram;
import tile.Program;

public class Tile {
    public static void main(String args[]) {
        tileParser parser = createTileParser("examples/test.tile");
        ParseTree ast = parser.program();
        AntlrToProgram programVisitor = new AntlrToProgram();
        Program program = programVisitor.visit(ast);

        program.generate();
    }

    private static tileParser createTileParser(String filePath) {
        tileParser parser = null;
        
        try {
            CharStream input = CharStreams.fromFileName(filePath);
            tileLexer lexer = new tileLexer(input);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            parser = new tileParser(tokens);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return parser;
    }
}
