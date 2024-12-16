package tile.ast.expr;

import java.util.List;

import tile.Program;
import tile.ast.base.Expression;
import tile.ast.types.TypeResolver;
import tile.ast.types.TypeResolver.TypeFuncCall;
import tile.sym.TasmSymbolGenerator;

public class FuncCallExpression implements Expression {

    TypeFuncCall typeInfo;
    private String funcId;
    private List<Expression> arg_exprs;
    private boolean is_native;

    public FuncCallExpression(String funcId, List<Expression> arg_exprs, TypeFuncCall typeInfo, boolean is_native) {
        this.funcId = funcId;
        this.arg_exprs = arg_exprs;
        this.typeInfo = typeInfo;
        this.is_native = is_native;
    }

    @Override
    public String generateTasm(String generatedCode) {
        String tasmFuncSym = TasmSymbolGenerator.tasmGenFunctionName(funcId);
        int native_index = -1;
        if (is_native == false) {
            Program.funcDefSymbols.get(tasmFuncSym).getArgs().size();
        } else {
            native_index = Program.nativeFuncDeclSymbols.get(funcId).getIndex();
            Program.nativeFuncDeclSymbols.get(funcId).getArgs().size();
        }
        
        // TODO: Be sure about not passing naked expressions when an arg declared as 'ref'!

        String callInstruction = "    call ";
        if (is_native) {
            tasmFuncSym = Integer.toString(native_index);
            callInstruction = "    native ";
        }

        for (int i = 0; i < arg_exprs.size(); i++) {
            generatedCode = arg_exprs.get(i).generateTasm(generatedCode);
        }
        generatedCode += callInstruction + tasmFuncSym;
        if (is_native) {
            generatedCode += " ; " + funcId;
        }
        generatedCode += "\n";

        // if variable is defined as ref:
        // TODO: implement passing as referance variables
        // for (int i = 0; i < args.size(); i++) {
        //     generatedCode = arg_exprs.get(i).generateTasm(generatedCode);
        // }

        return generatedCode;
    }

    @Override
    public String getType() {
        if (is_native) {
            return TypeResolver.CTypeConvert(this.typeInfo.result_type);
        }
        return this.typeInfo.result_type;
    }
    
}
