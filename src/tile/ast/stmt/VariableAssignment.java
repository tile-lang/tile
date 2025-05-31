package tile.ast.stmt;

import java.util.List;

import tile.ast.base.Expression;
import tile.ast.base.Statement;
import tile.ast.types.TypeResolver;
import tile.ast.types.TypeResolver.TypeInfoVariableDef;

public class VariableAssignment implements Statement {
    
    private TypeInfoVariableDef typeInfo;
    private String varId;
    private int tasmIdx;
    private Statement exprStmt;
    String assignmentOperator;
    private List<Expression> indicies;
    private boolean isGlobal;

    public VariableAssignment(TypeInfoVariableDef typeInfo, String varId, String assignmentOperator, List<Expression> indicies, Statement exprStmt, int tasmIdx) {
        this.typeInfo = typeInfo;
        this.varId = varId;
        this.exprStmt = exprStmt;
        this.tasmIdx = tasmIdx;
        this.assignmentOperator = assignmentOperator;
        this.indicies = indicies;
        isGlobal = false;
    }

    public int getTasmIdx() {
        return tasmIdx;
    }

    public String getType() {
        return typeInfo.var_type;
    }

    private String genLoadCode(String generatedCode) {
        if (isGlobal) {
            generatedCode += "    gload " + tasmIdx + "\n";
        } else {
            generatedCode += "    load " + tasmIdx + "\n";
        }
        return generatedCode;
    }

    private String generateTasmAssign(String generatedCode) {
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

        if (typeInfo.info_array == null && typeInfo.info_object == null) {
            if (isGlobal) {
                generatedCode += "    gstore " + tasmIdx + " ; " + typeInfo.var_type + " " + varId + "\n";
            } else {
                generatedCode += "    store " + tasmIdx + " ; " + typeInfo.var_type + " " + varId + "\n";
            }
        } else if (typeInfo.info_object != null) {

            String lastFieldId = typeInfo.info_object.fieldIds.getLast();
            int type_size = typeInfo.info_object.fields.get(lastFieldId).type_size;
            int offset = typeInfo.info_object.fields.get(lastFieldId).offset;

            generatedCode = genLoadCode(generatedCode);
            generatedCode += "    push " + offset + "\n";
            generatedCode += "    push " + type_size + "\n";
            generatedCode += "    hsetof\n";
        } else if (typeInfo.info_array != null) {
            generatedCode = genLoadCode(generatedCode);
            generatedCode = indicies.get(0).generateTasm(generatedCode);
            generatedCode += "    push " + typeInfo.info_array.element_size + "\n";
            generatedCode += "    hset\n";
        }

        return generatedCode;
    }

    private String generateTasmCompoundAssign(String generatedCode) {
        String operationType = "";
        if (typeInfo.info_array == null && typeInfo.info_object == null) {
            generatedCode = genLoadCode(generatedCode);
            operationType = typeInfo.var_type;
        } else if (typeInfo.info_object != null) {

            String lastFieldId = typeInfo.info_object.fieldIds.getLast();
            int type_size = typeInfo.info_object.fields.get(lastFieldId).type_size;
            int offset = typeInfo.info_object.fields.get(lastFieldId).offset;

            generatedCode = genLoadCode(generatedCode);
            generatedCode += "    deref ; dereferance\n";
            generatedCode += "    push " + offset + "\n";
            generatedCode += "    add\n";
            generatedCode += "    derefb " + type_size + " ; dereferance\n";

            operationType = typeInfo.info_object.fields.get(lastFieldId).type;

        } else if (typeInfo.info_array != null) {
            generatedCode = genLoadCode(generatedCode);
            generatedCode += "    deref ; dereferance\n";
            generatedCode = indicies.get(0).generateTasm(generatedCode);
            generatedCode += "    push " + typeInfo.info_array.element_size + "\n";
            generatedCode += "    mult\n";
            generatedCode += "    add\n";
            generatedCode += "    derefb "  + typeInfo.info_array.element_size + " ; dereferance\n";

            // TODO: make here dynamic when implement multi-dim arrays!
            operationType = TypeResolver.resolveArrayIndexAccessor(typeInfo.info_array.type, 1).type;
        }

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

        //  compound assignments
        if (assignmentOperator.equals("+=")) {
            if (operationType.equals("int")) {
                generatedCode += "    add\n";
            } else if (operationType.equals("float")) {
                generatedCode += "    addf\n";
            }
        } else if (assignmentOperator.equals("-=")) {
            if (operationType.equals("int")) {
                generatedCode += "    sub\n";
            } else if (operationType.equals("float")) {
                generatedCode += "    subf\n";
            }
        } else if (assignmentOperator.equals("*=")) {
            if (operationType.equals("int")) {
                generatedCode += "    mult\n";
            } else if (operationType.equals("float")) {
                generatedCode += "    multf\n";
            }
        } else if (assignmentOperator.equals("/=")) {
            if (operationType.equals("int")) {
                generatedCode += "    div\n";
            } else if (operationType.equals("float")) {
                generatedCode += "    divf\n";
            }
        }

        if (typeInfo.info_array == null && typeInfo.info_object == null) {
            if (isGlobal) {
                generatedCode += "    gstore " + tasmIdx + " ; " + typeInfo.var_type + " " + varId + "\n";
            } else {
                generatedCode += "    store " + tasmIdx + " ; " + typeInfo.var_type + " " + varId + "\n";
            }
        } else if (typeInfo.info_object != null) {

            String lastFieldId = typeInfo.info_object.fieldIds.getLast();
            int type_size = typeInfo.info_object.fields.get(lastFieldId).type_size;
            int offset = typeInfo.info_object.fields.get(lastFieldId).offset;

            generatedCode = genLoadCode(generatedCode);
            generatedCode += "    push " + offset + "\n";
            generatedCode += "    push " + type_size + "\n";
            generatedCode += "    hsetof\n";
        } else if (typeInfo.info_array != null) {
            generatedCode = genLoadCode(generatedCode);
            generatedCode = indicies.get(0).generateTasm(generatedCode);
            generatedCode += "    push " + typeInfo.info_array.element_size + "\n";
            generatedCode += "    hset\n";
        }

        return generatedCode;
    }

    @Override
    public String generateTasm(String generatedCode) {
        if (assignmentOperator.equals("=")) {
            generatedCode = generateTasmAssign(generatedCode);
        } else {
            generatedCode = generateTasmCompoundAssign(generatedCode);
        }

        return generatedCode;
    }
    
    public String getVarId() {
        return varId;
    }

    public void setAsGlobal() {
        isGlobal = true;
    }
}
