package tile.ast.stmt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tile.ast.base.Statement;
import tile.ast.types.TypeReslover.TypeFuncCall;
import tile.sym.TasmSymbolGenerator;

public class FunctionDefinition implements Statement {

    public static Map<String, FunctionDefinition> funcDefSymbols = new HashMap<>();

    public static class FuncArg {
        private String type;
        private String argId;
        private boolean is_ref;
        public FuncArg(String type, String argId, boolean is_ref) {
            this.type = type;
            this.argId = argId;
            this.is_ref = is_ref;
        }
        public String getType() {
            return type;
        }
        public String getArgId() {
            return argId;
        }
        public boolean isIs_ref() {
            return is_ref;
        }
    }

    private String funcId;
    private List<FuncArg> args;
    private Statement block;
    private TypeFuncCall return_type;

    public String getReturnType() {
        return return_type.result_type;
    }

    public FunctionDefinition(String funcId, List<FuncArg> args, TypeFuncCall return_type, Statement block) {
        this.funcId = funcId;
        this.args = args;
        this.return_type = return_type;
        this.block = block;

        String tasmFuncSym = TasmSymbolGenerator.tasmGenFunctionName(funcId);
        // add to the hash table to see if it is defined when call the function
        FunctionDefinition.funcDefSymbols.put(tasmFuncSym, this);
    }

    @Override
    public String generateTasm(String generatedCode) {
        String tasmFuncSym = TasmSymbolGenerator.tasmGenFunctionName(funcId);
        generatedCode += "proc " + tasmFuncSym + "\n";
        for (int i = 0; i < args.size(); i++) {
            generatedCode += "    ";
            generatedCode += "store " + (i) + " ;" + args.get(i).type + " " + args.get(i).argId + "\n";
        }
        generatedCode = block.generateTasm(generatedCode);
        
        // if it is referance push to stack back
        for (int i = 0; i < args.size(); i++) {
            if (args.get(i).is_ref) {
                generatedCode += "    ";
                generatedCode += "load " + (i) + "; ref " + args.get(i).type + " " + args.get(i).argId + "\n";
            }
        }
        generatedCode += "    ret\n";
        generatedCode += "endp\n";

        return generatedCode;
    }

    public String getFuncId() {
        return funcId;
    }

    public List<FuncArg> getArgs() {
        return args;
    }

    public TypeFuncCall getReturn_type() {
        return return_type;
    }
    
}
