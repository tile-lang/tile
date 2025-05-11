package tile.ast.stmt;

import tile.ast.base.Statement;

public class VariableDecleration implements Statement, Variable{

    private String typeInfo;
    private String varId;
    private int tasmIdx;
    private boolean isGlobal;


    public VariableDecleration(String typeInfo, String varId) {
        this.typeInfo = typeInfo;
        this.varId = varId;
        isGlobal = false;
    }

    public void setTasmIdx(int idx) {
        tasmIdx = idx;
    }

    public int getTasmIdx() {
        return tasmIdx;
    }

    public String getType() {
        return typeInfo;
    }

    public String getVarId() {
        return varId;
    }

    @Override
    public String generateTasm(String generatedCode) {
        // 0 for all defined types
        // user defined null (0)
        if (isGlobal) {
            // generatedCode += "push 0\n";
            // generatedCode += "gstore " + tasmIdx + " ; " + typeInfo + " " + varId + "\n";
        } else {
            generatedCode += "    push 0\n";
            generatedCode += "    store " + tasmIdx + " ; " + typeInfo + " " + varId + "\n";
        }

        return generatedCode;
    }

    @Override
    public void setAsGlobal() {
        isGlobal = true;
    }
}
