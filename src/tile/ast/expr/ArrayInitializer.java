package tile.ast.expr;

import tile.ast.base.Expression;
import tile.ast.types.TypeResolver.TypeInfoArray;

public class ArrayInitializer implements Expression {

    private TypeInfoArray typeInfo;
    private int[] arrSizes;

    public ArrayInitializer(TypeInfoArray typeInfo, int[] arrSizes) {
        this.typeInfo = typeInfo;
        this.arrSizes = arrSizes;
    }

    @Override
    public String generateTasm(String generatedCode) {
        int size_in_bytes = typeInfo.element_size;
        for (int i = 0; i < arrSizes.length; i++) {
            size_in_bytes *= arrSizes[i];
        }
        generatedCode += "    ; sized arr initializer\n";
        generatedCode += "    push " + size_in_bytes + "\n";
        generatedCode += "    push 0\n";
        generatedCode += "    halloc\n";

        return generatedCode;
    }

    @Override
    public String getType() {
        return typeInfo.type;
    }
    
}
