package tile;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tile.ast.base.Statement;
import tile.ast.stmt.BlockStmt;
import tile.ast.stmt.FunctionDefinition;
import tile.ast.stmt.NativeFunctionDecl;
import tile.ast.stmt.VariableDefinition;
import tile.ast.base.Generator;

public class Program extends Generator {
    private List<Statement> statements;
    public static Map<String, FunctionDefinition> funcDefSymbols = new HashMap<>();
    public static Map<String, NativeFunctionDecl> nativeFuncDeclSymbols = new HashMap<>();
    public static List<BlockStmt> blockStack = new ArrayList<>();
    public static Map<String, VariableDefinition> globalVariableSymbols = new HashMap<>();

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
        generatedCode += "push 0 ; argc\n"; // for simulating argc and argv for now
        // generatedCode += "push 0 ; argv\n";
        generatedCode += "call func_main_\n";
        generatedCode += "\n";
        generatedCode += "hlt\n";

        return generatedCode;
    }

    public String pushBackGeneratedCode(String code) {
        generatedCode += code;
        return generatedCode;
    }

    public String pushForwardGeneratedCode(String code) {
        generatedCode = code + generatedCode;
        return generatedCode;
    }

    private void writeOutput(String outputPath) {
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(outputPath, "ASCII");
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
    }

    public void write(String outputPath) {
        // debug:
        System.out.println(generatedCode);
        // write to a file
        writeOutput(outputPath);
    }
    
}
