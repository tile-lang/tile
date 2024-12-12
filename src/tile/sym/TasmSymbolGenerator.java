package tile.sym;

public class TasmSymbolGenerator {

    private static String FUNCTION_SYM_PREFIX = "func_";
    private static String VARIABLE_SYM_PREFIX = "var_";

    //TODO: make it able to overloadable!
    public static String tasmGenFunctionName(String funcId) {
        return FUNCTION_SYM_PREFIX + funcId + "_";
    }

    public static String tasmGenVariableName(int blockId, String varId) {
        String blockStr = Integer.toString(blockId);
        return VARIABLE_SYM_PREFIX + varId + "_" + blockStr;
    }
}
