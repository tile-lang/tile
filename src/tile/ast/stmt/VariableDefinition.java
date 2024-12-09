package tile.ast.stmt;

import java.util.HashMap;
import java.util.Map;

import tile.ast.base.Statement;
import tile.ast.types.TypeResolver.TypeInfoVariableDef;

public class VariableDefinition implements Statement {
    
    private TypeInfoVariableDef typeInfo;
    private String varId;
    private int tasmIdx;
    private Statement exprStmt;

    public VariableDefinition(TypeInfoVariableDef typeInfo, String varId, Statement exprStmt) {
        this.typeInfo = typeInfo;
        this.varId = varId;
        this.exprStmt = exprStmt;
    }

    public void setTasmIdx(int idx) {
        tasmIdx = idx;
    }

    @Override
    public String generateTasm(String generatedCode) {
        String exprType = ((ExpressionStmt)exprStmt).getType();

        if (exprStmt != null)
            generatedCode = ((ExpressionStmt)exprStmt).generateTasm(generatedCode);

        if (typeInfo.auto_cast == true) {
            if (typeInfo.var_type.equals("int") && typeInfo.expr_type.equals("float")) {
                generatedCode += "    ; auto cast float to int\n";
                generatedCode += "    ";
                generatedCode += "cf2i\n";
                typeInfo.result_type = "int";
            }
            else if (typeInfo.var_type.equals("float") && typeInfo.expr_type.equals("int")) {
                generatedCode += "    ; auto cast int to float\n";
                generatedCode += "    ";
                generatedCode += "ci2f\n";
                typeInfo.result_type = "float";
            }
        }


        generatedCode += "    ";
        generatedCode += "store " + tasmIdx + " ; " + typeInfo.var_type + " " + varId + "\n";

        return generatedCode;
    }
    
    public String getVarId() {
        return varId;
    }
}
