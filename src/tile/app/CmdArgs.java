package tile.app;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class CmdArgs {

    public static class ArgResults {
        String inputFile = null;
        String outputFile = "out.tasm";
        String module = null;
    }

    public static ArgResults parseCmdArgs(String[] args) {
        ArgResults results = new ArgResults();
        boolean showHelp = false;
        boolean showVersion = false;

        List<String> argList = Arrays.asList(args);
        Iterator<String> iterator = argList.iterator();

        while (iterator.hasNext()) {
            String arg = iterator.next();

            switch (arg) {
                case "-o":
                    if (iterator.hasNext()) {
                        results.outputFile = iterator.next();
                    } else {
                        throw new IllegalArgumentException("Error: Missing output file after -o");
                    }
                    break;
                case "-l":
                    if (iterator.hasNext()) {
                        results.module = iterator.next();
                    } else {
                        throw new IllegalArgumentException("Error: Missing module name after -l");
                    }
                    break;
                case "-h":
                    showHelp = true;
                    break;
                case "-v":
                case "-version":
                case "--v":
                case "--version":
                    showVersion = true;
                    break;
                default:
                    if (results.inputFile == null) {
                        results.inputFile = arg;
                    } else {
                        throw new IllegalArgumentException("Error: Multiple input files specified.");
                    }
                    break;
            }
        }

        if (showHelp) {
            System.out.println("Usage: tile <input> [-o <output>] [-l <dynamic-library>]\n [-h]\n [-v | --v | -version | --version]");
            System.exit(0);
        }

        if (showVersion) {
            System.out.println("0.0.1");
            System.exit(0);
        }

        if (results.inputFile == null) {
            throw new IllegalArgumentException("Error: Input file is required.");
        }

        return results;
    }
}
