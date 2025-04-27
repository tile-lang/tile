package tile.app;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class CmdArgs {

    public static class ArgResults {
        String inputFile = null;
        String outputFile = "out.tasm";
        String module = null;
        boolean gen_tasm = false;
        boolean debug = false;
    }

    private static void tileUsage() {
        Log.info("Tile Usage:\n\ttile <input> [-o <output>] [-l <dynamic-library>]\n\t[-h]\n\t[-v | --v | -version | --version]\n\t[-gen-tasm]");
    }

    private static void tileHelp() {
        Log.info("Tile Usage:\n" +
         "\ttile <input> [-o <output>] [-l <dynamic-library>] [-h] [-v | --v | -version | --version] [-gen-tasm]\n" +
         "\t<input>             : Input Tile source file (required)\n" +
         "\t-o <output>        : Output binary file name (default: based on input)\n" +
         "\t-l <dynamic-library> : Link with specified dynamic library\n" +
         "\t-h                 : Show this help message\n" +
         "\t-v, --v, -version, --version : Show version information\n" +
         "\t-gen-tasm          : Keep the intermediate TASM file");
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
                        Log.error("Missing output file after -o");
                        tileUsage();
                        throw new IllegalArgumentException();
                    }
                    break;
                case "-l":
                    if (iterator.hasNext()) {
                        results.module = iterator.next();
                    } else {
                        Log.error("Missing module name after -l");
                        tileUsage();
                        throw new IllegalArgumentException();
                    }
                    break;
                case "-gen-tasm":
                    results.gen_tasm = true;
                    break;
                case "-dev":
                case "--dev":
                    results.debug = true;
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
                        Log.error("Multiple input files specified.");
                        throw new IllegalArgumentException();
                    }
                    break;
            }
        }

        if (showHelp) {
            tileHelp();
            System.exit(0);
        }

        if (showVersion) {
            Log.info("0.0.1");
            System.exit(0);
        }

        if (results.inputFile == null) {
            Log.error("Input file is required.");
            tileUsage();
            throw new IllegalArgumentException();
        }

        return results;
    }
}
