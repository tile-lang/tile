package tile.app;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import org.antlr.v4.runtime.tree.ParseTree;

import tile.err.TileErrorListener;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

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

        // syntax errors
        TileErrorListener errorListener = new TileErrorListener();
        parser.removeErrorListeners();
        parser.addErrorListener(errorListener);


        ParseTree ast = parser.program();

        // check for syntax errors
        if (errorListener.hasErrors()) {
            System.out.println("Parsing failed with the following errors:");
            for (String error : errorListener.getErrorMessages()) {
                System.out.println(error);
            }
            System.exit(2);
        }

        AntlrToProgram programVisitor = new AntlrToProgram();
        Program.programPaths.add(getProgramDir(results));
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

    public static tileParser createTileParser(String filePath) {
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
            Log.error(filePath + " file cannot found!");
            System.exit(3);
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

    private static Path getProgramDir(ArgResults results) {
        Path filePath = Paths.get(System.getProperty("user.dir"), results.inputFile);
        Path dir = filePath.getParent();
        return dir != null ? dir : Paths.get(System.getProperty("user.dir"));
    }
}
