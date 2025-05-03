package tile.ast.expr;

import java.util.List;

import tile.ast.base.Expression;
import tile.ast.types.TypeResolver.TypeInfoArray;

public class ArrayIndexAccessor implements Expression {

    TypeInfoArray typeInfo;
    private int identifierTasmIdx;
    List<Expression> indicies;
    boolean isGlobal;

    public ArrayIndexAccessor(TypeInfoArray typeInfo, int identifierTasmIdx, List<Expression> indicies) {
        this.typeInfo = typeInfo;
        this.identifierTasmIdx = identifierTasmIdx;
        this.indicies = indicies;
        isGlobal = false;
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
        generatedCode = genLoadCode(generatedCode);
        generatedCode += "    deref ; dereferance\n";
        generatedCode += "    derefb "  + typeInfo.element_size + " ; dereferance\n";
        generatedCode = indicies.get(0).generateTasm(generatedCode);
        generatedCode += "    push " + typeInfo.element_size + "\n";
        generatedCode += "    mult\n";
        generatedCode += "    add\n";
        return generatedCode;
        
        
        // generatedCode += "    derefb "  + typeInfo.element_size + " ; dereferance\n";
        // generatedCode += "    deref ; dereferance\n";
    }

    @Override
    public String getType() {
        return typeInfo.type;
    }

    public void setAsGlobal() {
        isGlobal = true;
    }
    
}
