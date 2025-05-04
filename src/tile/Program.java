package tile;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tile.ast.base.Statement;
import tile.ast.stmt.BlockStmt;
import tile.ast.stmt.FunctionDefinition;
import tile.ast.stmt.NativeFunctionDecl;
import tile.ast.stmt.Variable;
import tile.app.Log;
import tile.ast.base.Generator;

public class Program extends Generator {
    private List<Statement> statements;
    public static Map<String, FunctionDefinition> funcDefSymbols = new HashMap<>();
    public static Map<String, NativeFunctionDecl> nativeFuncDeclSymbols = new HashMap<>();
    public static List<BlockStmt> blockStack = new ArrayList<>();
    private static int tasmGlobalVarIdx = 0;
    public static Map<String, Variable> globalVariableSymbols = new HashMap<>();
    public static List<Statement> globalVariables = new ArrayList<Statement>();
    public static Deque<Statement> parentStack = new ArrayDeque<>();
    public static List<Path> programPaths = new ArrayList<>();
    private static boolean _err;

    private Path baseDirectory;
    private boolean isImportedFile = false;

    public Program() {
        super();
        _err = false;
        statements = new ArrayList<Statement>();
    }

    public static void setError() {
        _err = true;
    }

    public static boolean getError() {
        return _err;
    }

    public void setBaseDirectory(Path baseDirectory) {
        this.baseDirectory = baseDirectory;
    }

    public Path getBaseDirectory() {
        return baseDirectory;
    }

    public void markAsImported() {
        this.isImportedFile = true;
    }

    public void addStatement(Statement stmt) {
        statements.add(stmt);
    }

    private String generateProgram(String generatedCode) {
        if (!isImportedFile) {
            generatedCode += "; program begins\n";
            generatedCode += "jmp __start\n";
            generatedCode += "\n";
        }

        for (int i = 0; i < statements.size(); i++) {
            Statement stmt = statements.get(i);
            if (stmt != null) {
                if (stmt instanceof Variable) {
                    globalVariables.add(stmt);
                } else {
                    generatedCode = stmt.generateTasm(generatedCode);
                }
            }
        }

        if(!isImportedFile) {
            generatedCode += "__start:\n";
            generatedCode += "; global variables\n";
            for (int i = 0; i < globalVariables.size(); i++) {
                Statement stmt = globalVariables.get(i);
                if (stmt != null) {
                    generatedCode = stmt.generateTasm(generatedCode);
                }
            }
        
            generatedCode += "\n\n";
            generatedCode += "push 0 ; argc\n"; // for simulating argc and argv for now
            // generatedCode += "push 0 ; argv\n";
            generatedCode += "call func_main_\n";
            generatedCode += "\n";
            generatedCode += "hlt\n";
        }
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
        Log.debug(generatedCode);
        // write to a file
        writeOutput(outputPath);
    }

    public static int getTasmVarIdx() {
        return tasmGlobalVarIdx++;
    }
    
}
