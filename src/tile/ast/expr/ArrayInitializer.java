package tile.ast.expr;

import java.util.List;

import tile.ast.base.Expression;
import tile.ast.types.TypeResolver.TypeInfoArray;

public class ArrayInitializer implements Expression {

    private TypeInfoArray typeInfo;
    private List<Expression> arrSizes;

    public ArrayInitializer(TypeInfoArray typeInfo, List<Expression> arrSizes) {
        this.typeInfo = typeInfo;
        this.arrSizes = arrSizes;
    }

    @Override
    public String generateTasm(String generatedCode) {
        int size_in_bytes = typeInfo.element_size;
        generatedCode += "    ; sized arr initializer\n";
        for (int i = 0; i < arrSizes.size(); i++) {
            generatedCode = arrSizes.get(i).generateTasm(generatedCode);
            generatedCode += "    push " + size_in_bytes + "\n";
            generatedCode += "    mult\n";
            generatedCode += "    push 0\n";
            generatedCode += "    halloc\n";
        }

        return generatedCode;
    }

    @Override
    public String getType() {
        return typeInfo.type;
    }
    
}
