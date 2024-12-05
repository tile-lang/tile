package tile;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import tile.ast.base.Statement;
import tile.ast.base.Generator;

public class Program extends Generator {
    private List<Statement> statements;

    public Program() {
        super();
        statements = new ArrayList<Statement>();
    }

    public void addStatement(Statement stmt) {
        statements.add(stmt);
    }

    private String generateProgram(String generatedCode) {
        generatedCode += "; program begins\n";
        generatedCode += "jmp __start\n";
        generatedCode += "\n";

        for (int i = 0; i < statements.size(); i++) {
            generatedCode = statements.get(i).generateTasm(generatedCode);
        }

        generatedCode += "__start:\n";
        generatedCode += "\n";
        generatedCode += "call main\n";
        generatedCode += "\n";
        generatedCode += "hlt\n";

        return generatedCode;
    }

    private void writeOutput() {
        PrintWriter writer = null;
        try {
            writer = new PrintWriter("examples/test.tasm", "ASCII");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        writer.print(generatedCode);
        writer.close();
    }

    @Override
    public void generate() {
        
        generatedCode = generateProgram(generatedCode);

        // debug:
        System.out.println(generatedCode);
        // write to a file
        writeOutput();
    }
    
}
