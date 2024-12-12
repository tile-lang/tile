package tile.ast.stmt;

import java.util.List;

import tile.ast.base.Statement;
import tile.ast.types.TypeResolver.TypeFuncCall;
import tile.sym.TasmSymbolGenerator;

public class FunctionDefinition implements Statement {

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

    private int tasmVarIdx;

    public String getReturnType() {
        return return_type.result_type;
    }

    public FunctionDefinition(String funcId, List<FuncArg> args, TypeFuncCall return_type, Statement block) {
        this.funcId = funcId;
        this.args = args;
        this.return_type = return_type;
        this.block = block;
        tasmVarIdx = 0;//args.size();
    }

    @Override
    public String generateTasm(String generatedCode) {
        String tasmFuncSym = TasmSymbolGenerator.tasmGenFunctionName(funcId);
        generatedCode += "proc " + tasmFuncSym + "\n";
        for (int i = 0; i < args.size(); i++) {
            generatedCode += "    ";
            generatedCode += "store " + (i) + " ; param " + args.get(i).type + " " + args.get(i).argId + "\n";
        }
        generatedCode = block.generateTasm(generatedCode);
        
        // if it is referance push to stack back
        for (int i = 0; i < args.size(); i++) {
            if (args.get(i).is_ref) {
                generatedCode += "    ";
                generatedCode += "load " + (i) + "; param ref " + args.get(i).type + " " + args.get(i).argId + "\n";
            }
        }

        if (return_type.result_type.equals("void")) {
            generatedCode += "    ret\n";
        }
        generatedCode += "endp\n\n";

        return generatedCode;
    }

    public void setBlockStmt(BlockStmt block) {
        this.block = block;
    }

    public BlockStmt getBlockStmt() {
        return (BlockStmt)block;
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

    public int getTasmVarIdx() {
        return tasmVarIdx++;
    }
    
}
