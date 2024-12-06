package tile.ast.expr;

import java.util.List;
import tile.ast.base.Expression;
import tile.ast.stmt.FunctionDefinition;
import tile.ast.types.TypeReslover.TypeFuncCall;
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
        FunctionDefinition.funcDefSymbols.get(tasmFuncSym).getArgs().size();
        
        // TODO: Be sure about not passing naked expressions when an arg declared as 'ref'!

        String callInstruction = "    call ";
        if (is_native) {
            callInstruction = "    native ";
        }

        for (int i = 0; i < arg_exprs.size(); i++) {
            generatedCode = arg_exprs.get(i).generateTasm(generatedCode);
        }
        generatedCode += callInstruction + tasmFuncSym + "\n";
        return generatedCode;
    }

    @Override
    public String getType() {
        return this.typeInfo.result_type;
    }
    
}
