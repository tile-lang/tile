package tile.ast.stmt;

import java.util.List;

import tile.ast.base.Statement;
import tile.ast.stmt.FunctionDefinition.FuncArg;
import tile.ast.types.TypeResolver;
import tile.ast.types.TypeResolver.TypeFuncCall;

public class NativeFunctionDecl implements Statement {

    private String funcId;
    private List<FuncArg> args;
    private TypeFuncCall return_type;

    private static int nativeFuncIndex = 0;

    private int index;

    public int getIndex() {
        return index;
    }

    public NativeFunctionDecl(String funcId, List<FuncArg> args, TypeFuncCall return_type) {
        this.funcId = funcId;
        this.args = args;
        this.return_type = return_type;
        index = nativeFuncIndex++;
    }

    @Override
    public String generateTasm(String generatedCode) {
        // example:
        // @cfun void InitWindow i32 i32 ptr
        
        generatedCode += "@cfun ";
        generatedCode += return_type.result_type.substring(1) + " "; // BE AWARE
        generatedCode += funcId + " ";
        
        for (int i = 0; i < args.size(); i++) {
            generatedCode += args.get(i).getType().substring(1) + " "; // BE AWARE
        }
        generatedCode += "\n";
        
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

    public String getReturnType() {
        // System.out.println(return_type.result_type);
        // return return_type.result_type;
        // return_type.result_type = resultTypeConvert(return_type.result_type);
        // return return_type.result_type;
        return TypeResolver.CTypeConvert(return_type.result_type);
    }
    
}
