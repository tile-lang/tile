package tile.app;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;

import gen.antlr.tile.tileLexer;
import gen.antlr.tile.tileParser;
import tile.AntlrToProgram;
import tile.PrePassStatement;
import tile.Program;
import tile.app.CmdArgs.ArgResults;

public class Tile {
    public static void main(String args[]) {

        // command line arguments
        ArgResults results = null;
        try {
            results = CmdArgs.parseCmdArgs(args);
            Log.debug("Input File: " + results.inputFile);
            Log.debug("Output File: " + results.outputFile);
            Log.debug("Module: " + (results.module != null ? results.module : "Not specified"));
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }

        Log.setDebugMode(results.debug);

        tileParser parser = createTileParser(results.inputFile);
        ParseTree ast = parser.program();
        AntlrToProgram programVisitor = new AntlrToProgram();
        Program program = programVisitor.visit(ast);

        if (Program.getError() == false) {
            program.generate();
            
            PrePassStatement prePassVisitor = new PrePassStatement(program);
            prePassVisitor.visit(ast);
    
            String tasmInput = getFileNameWithoutExtension(results.outputFile) + ".tasm";
            String tasmOutput = getFileNameWithoutExtension(results.outputFile) + ".bin";

            
            program.write(tasmInput);
            if (results.module != null) {
                callTasmCompiler(new String[] {"tasm", tasmInput, "-o", tasmOutput, "-l", results.module});
            } else {
                callTasmCompiler(new String[] {"tasm", tasmInput, "-o", tasmOutput});
            }

            if (results.gen_tasm == false) {
                new File(tasmInput).delete();
            }
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

    private static void callTasmCompiler(String[] commands) {
        ProcessBuilder processBuilder = new ProcessBuilder(Arrays.asList(commands));
         try {
            // Start the process
            Process process = processBuilder.start();

            // Capture and print the output
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            }

            // Capture and print the error stream (if any)
            try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = errorReader.readLine()) != null) {
                    System.err.println(line);
                }
            }


            // Wait for the process to complete
            int exitCode = process.waitFor();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static String getFileNameWithoutExtension(String filePath) {
        File file = null;
        try {
            file = new File(filePath);
        } catch (Exception e) {
            Log.error("\"" + filePath + "\""  + "no such a directory, try to mkdir first!");
        }
        
        // Get the file name with extension
        String fileName = filePath;
        
        // Get the file name without extension
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0) {
            fileName = fileName.substring(0, dotIndex);
        }
        
        return fileName;
    }
}
