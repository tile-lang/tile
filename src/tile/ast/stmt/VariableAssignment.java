package tile.ast.stmt;

import tile.ast.base.Statement;
import tile.ast.types.TypeResolver.TypeInfoVariableDef;

public class VariableAssignment implements Statement {
    
    private TypeInfoVariableDef typeInfo;
    private String varId;
    private int tasmIdx;
    private Statement exprStmt;
    String assignmentOperator;

    public VariableAssignment(TypeInfoVariableDef typeInfo, String varId, String assignmentOperator, Statement exprStmt, int tasmIdx) {
        this.typeInfo = typeInfo;
        this.varId = varId;
        this.exprStmt = exprStmt;
        this.tasmIdx = tasmIdx;
        this.assignmentOperator = assignmentOperator;
    }

    public int getTasmIdx() {
        return tasmIdx;
    }

    public String getType() {
        return typeInfo.var_type;
    }

    @Override
    public String generateTasm(String generatedCode) {
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
