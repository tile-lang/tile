package tile.sym;

import tile.Program;
import tile.ast.stmt.BlockStmt;

public class TasmSymbolGenerator {

    private static final String FUNCTION_SYM_PREFIX = "func_";
    private static final String VARIABLE_SYM_PREFIX = "var_";
    private static final String GLOBAL_VARIABLE_SYM_PREFIX = "global_";

    //TODO: make it able to overloadable!
    public static String tasmGenFunctionName(String funcId) {
        return FUNCTION_SYM_PREFIX + funcId + "_";
    }

    public static String tasmGenVariableName(int blockId, String varId) {
        String blockStr = Integer.toString(blockId);
        return VARIABLE_SYM_PREFIX + varId + "_" + blockStr;
    }

    public static String tasmGenGlobalVariableName(String varId) {
        return GLOBAL_VARIABLE_SYM_PREFIX + VARIABLE_SYM_PREFIX + varId;
    }

    public static int identifierScopeFind(final String identifier, StringBuilder varType) throws Exception {
        BlockStmt blck = null;               
        int counter = 0;
        while (blck == null) {
            int index  = (Program.blockStack.size() - 1) - counter;
            if (index >= 0) {
                blck = Program.blockStack.get(index);
            } else {
                break;
            }
            counter++;
        }

        int blockId = blck.getBlockId();
        String tasmVarSym = TasmSymbolGenerator.tasmGenVariableName(blockId, identifier);

        // FIXME: allow variable def outside functions!

        counter = 0;
        while (blck.variableSymbols.get(tasmVarSym) == null) {
            int index  = (Program.blockStack.size() - 1) - counter;
            if (index >= 0) {
                // System.out.println("bid: " + blockId);
                blck = Program.blockStack.get(index);
                blockId = blck.getBlockId();
                tasmVarSym = TasmSymbolGenerator.tasmGenVariableName(blockId, identifier);
            } else {
                break;
            }
            counter++;
        }


        int tasmIdx = -1;
        varType.setLength(0);

        // TODO:
        // if still cannot find the variable outer blocks look at the global scope

        try {
            tasmIdx = blck.variableSymbols.get(tasmVarSym).getTasmIdx();
            varType.append(blck.variableSymbols.get(tasmVarSym).getType());
        } catch (Exception e) {
            throw new RuntimeException();
        }

        return tasmIdx;
    }
}
