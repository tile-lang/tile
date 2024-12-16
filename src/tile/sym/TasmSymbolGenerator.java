package tile.sym;

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
}
