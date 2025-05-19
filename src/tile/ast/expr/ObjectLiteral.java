package tile.ast.expr;

import java.util.Map;

import tile.ast.base.Expression;
import tile.ast.stmt.TypeDefinition;

public class ObjectLiteral implements Expression {

    private String type;
    private TypeDefinition typeDef;
    private int[] assignedFields;

    public ObjectLiteral(TypeDefinition typeDef, String type, int[] assignedFields) {
        this.type = type;
        this.typeDef = typeDef;
        this.assignedFields = assignedFields;
    }

    @Override
    public String generateTasm(String generatedCode) {
        // allocate
        int totalSize = 0;
        for (Map.Entry<String, TypeDefinition.Field> entry : typeDef.getFields().entrySet()) {
            totalSize += entry.getValue().type_size;
        }
        generatedCode += "    ; type " + type + "\n";
        generatedCode += "    push " + totalSize + "\n";
        generatedCode += "    push 0\n";
        generatedCode += "    halloc\n";
        // set
        /*
        push 50
        load 1
        push 0
        push 4
        hset
        FIXME: fix here!
        for (int i = 0; i < typeDef.getFields().size(); i++) {
            if (assignedFields[i] == 1) {
                
            }

            int size = typeDef.getFields().get(i).type_size;
            generatedCode += "    push " + size + "\n";
            generatedCode += "    hset\n";
        }
        */

        return generatedCode;
    }

    @Override
    public String getType() {
        return type;
    }
    
}
