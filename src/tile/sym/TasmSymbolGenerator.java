package tile.sym;

public class TasmSymbolGenerator {

    private static String FUNCTION_SYM_PREFIX = "func_";

    //TODO: make it able to overloadable!
    public static String tasmGenFunctionName(String funcId) {
        return FUNCTION_SYM_PREFIX + funcId + "_";
    }
}
