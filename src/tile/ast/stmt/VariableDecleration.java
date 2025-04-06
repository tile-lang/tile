package tile.ast.stmt;

import tile.ast.base.Statement;

public class VariableDecleration implements Statement, Variable{

    private String typeInfo;
    private String varId;
    private int tasmIdx;

    public VariableDecleration(String typeInfo, String varId) {
        this.typeInfo = typeInfo;
        this.varId = varId;
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
        generatedCode += "    push 0\n";
        generatedCode += "    store " + tasmIdx + " ; " + typeInfo + " " + varId + "\n";

        return generatedCode;
    }
}
