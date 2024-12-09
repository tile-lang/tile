package tile.sym;

public class TasmSymbolGenerator {

    private static String FUNCTION_SYM_PREFIX = "func_";
    private static String VARIABLE_SYM_PREFIX = "var_";
    private static int tasmVariableIdx = 0;

    //TODO: make it able to overloadable!
    public static String tasmGenFunctionName(String funcId) {
        return FUNCTION_SYM_PREFIX + funcId + "_";
    }

    public static String tasmGenVariableName(String funcId, String varId) {
        return tasmGenFunctionName(funcId) + VARIABLE_SYM_PREFIX + varId;
    }
}
