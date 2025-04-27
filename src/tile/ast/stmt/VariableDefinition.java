package tile.ast.stmt;

import tile.ast.base.Statement;
import tile.ast.types.TypeResolver.TypeInfoVariableDef;

public class VariableDefinition implements Statement, Variable {
    
    private TypeInfoVariableDef typeInfo;
    private String varId;
    private int tasmIdx;
    private Statement exprStmt;
    private boolean isGlobal;

    public VariableDefinition(TypeInfoVariableDef typeInfo, String varId, Statement exprStmt) {
        this.typeInfo = typeInfo;
        this.varId = varId;
        this.exprStmt = exprStmt;
        isGlobal = false;
    }

    public void setTasmIdx(int idx) {
        tasmIdx = idx;
    }

    public int getTasmIdx() {
        return tasmIdx;
    }

    public String getType() {
        return typeInfo.var_type;
    }

    public String getVarId() {
        return varId;
    }

    @Override
    public String generateTasm(String generatedCode) {
        if (exprStmt != null) {
            generatedCode = ((ExpressionStmt)exprStmt).generateTasm(generatedCode);

            if (typeInfo.auto_cast == true) {
                if (typeInfo.var_type.equals("int") && typeInfo.expr_type.equals("float")) {
                    generatedCode += "    ; auto cast float to int\n";
                    generatedCode += "    cf2i\n";
                    typeInfo.result_type = "int";
                } else if (typeInfo.var_type.equals("float") && typeInfo.expr_type.equals("int")) {
                    generatedCode += "    ; auto cast int to float\n";
                    generatedCode += "    ci2f\n";
                    typeInfo.result_type = "float";
                }
            }

            if (isGlobal) {
                generatedCode += "    gstore " + tasmIdx + " ; " + typeInfo.var_type + " " + varId + "\n";
            } else {
                generatedCode += "    store " + tasmIdx + " ; " + typeInfo.var_type + " " + varId + "\n";
            }
        }

        return generatedCode;
    }

    @Override
    public void setAsGlobal() {
        isGlobal = true;
    }

}
