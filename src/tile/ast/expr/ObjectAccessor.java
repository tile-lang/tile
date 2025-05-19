package tile.ast.expr;

import tile.ast.base.Expression;
import tile.ast.stmt.TypeDefinition;

public class ObjectAccessor implements Expression {
    private TypeDefinition typeDefinition;
    private int identifierTasmIdx;
    boolean isGlobal = false;
    private String fieldId;
    ObjectAccessor accessor;

    public void setAsGlobal() {
        this.isGlobal = true;
    }

    public ObjectAccessor(String fieldId, TypeDefinition typeDefinition, int identifierTasmIdx, ObjectAccessor accessor) {
        this.identifierTasmIdx = identifierTasmIdx;
        this.accessor = accessor;
        this.typeDefinition = typeDefinition;
        this.fieldId = fieldId;
    }

    private String genLoadCode(String generatedCode) {
        if (isGlobal) {
            generatedCode += "    gload " + identifierTasmIdx + "\n";
        } else {
            generatedCode += "    load " + identifierTasmIdx + "\n";
        }
        return generatedCode;
    }

    @Override
    public String generateTasm(String generatedCode) {
        int size = typeDefinition.getFields().get(fieldId).type_size;
        int offset = typeDefinition.getFields().get(fieldId).offset;

        generatedCode = genLoadCode(generatedCode);
        generatedCode += "    deref ; dereferance\n";
        generatedCode += "    push " + offset + "\n";
        generatedCode += "    add\n";
        generatedCode += "    derefb " + size + " ; dereferance\n";
        return generatedCode;
    }

    @Override
    public String getType() {
        return typeDefinition.getFields().get(fieldId).type;
    }
}
