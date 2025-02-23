package tile.ast.expr;

import java.util.List;

import tile.ast.base.Expression;
import tile.ast.types.TypeResolver.TypeInfoArray;

public class ArrayIndexAccessor implements Expression {

    TypeInfoArray typeInfo;
    private int identifierTasmIdx;
    List<Expression> indicies;

    public ArrayIndexAccessor(TypeInfoArray typeInfo, int identifierTasmIdx, List<Expression> indicies) {
        this.typeInfo = typeInfo;
        this.identifierTasmIdx = identifierTasmIdx;
        this.indicies = indicies;
    }

    @Override
    public String generateTasm(String generatedCode) {
        generatedCode += "    load " + identifierTasmIdx + "\n";
        generatedCode += "    deref ; dereferance\n";
        // FIXME:
        generatedCode = indicies.get(0).generateTasm(generatedCode);
        generatedCode += "    push " + typeInfo.element_size + "\n";
        generatedCode += "    mult\n";
        generatedCode += "    add\n";
        generatedCode += "    deref ; dereferance\n";
        return generatedCode;
    }

    @Override
    public String getType() {
        return typeInfo.type;
    }
    
}
