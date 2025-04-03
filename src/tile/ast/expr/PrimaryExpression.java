package tile.ast.expr;

import tile.ast.base.Expression;
import tile.ast.types.TypeResolver;

public class PrimaryExpression implements Expression {

    private String value;
    private String type;
    private String unaryOp;
    private boolean isIdentifier;
    private int identifierTasmIdx;
    private int dataTasmIdx;

    public PrimaryExpression(String unaryOp, String value, String type, boolean isIdentifier, int tasmIdx, int dataTasmIdx) {
        if (unaryOp != null) {
            this.value = unaryOp + value;
        } else {
            this.value = value;
        }
        this.type = type;
        this.isIdentifier = isIdentifier;
        this.identifierTasmIdx = tasmIdx;
        this.dataTasmIdx = dataTasmIdx;
        this.unaryOp = unaryOp;
    }

    @Override
    public String getType() {
        return type;
    }

    private String generateTasmForPrimitive(String generatedCode) {
        generatedCode += "    ";
        if (isIdentifier) {
            if (unaryOp != null) {
                if (unaryOp.equals("-")) {
                    if (type.equals("int")) {
                        generatedCode += "    ";
                        generatedCode += "push 0" + "\n";
                    } else if (type.equals("float")) {
                        generatedCode += "    ";
                        generatedCode += "push 0.0" + "\n";
                    }
                }
            }
            generatedCode += "load " + identifierTasmIdx + "\n";
            if (unaryOp != null) {
                if (unaryOp.equals("-")) {
                    if (type.equals("int")) {
                        generatedCode += "    ";
                        generatedCode += "sub" + "\n";
                    } else if (type.equals("float")) {
                        generatedCode += "    ";
                        generatedCode += "subf" + "\n";
                    }
                }
            }
        } else {
            generatedCode += "push " + value + "\n";
        }
        return generatedCode;
    }

    public String generateTasmForString(String generatedCode) {
        // FIXME: generate indicies
        generatedCode += "    aloadc " + dataTasmIdx + "\n";
        return generatedCode;
    }

    @Override
    public String generateTasm(String generatedCode) {
        System.out.println("primary type:" + type);
        if (TypeResolver.isNumericType(type) || TypeResolver.isCharType(type) || TypeResolver.isBoolType(type)) {
            generatedCode = generateTasmForPrimitive(generatedCode);
        } else if (TypeResolver.isStringType(type)) {
            generatedCode = generateTasmForString(generatedCode);
        } else {
            generatedCode += "    load " + identifierTasmIdx + "\n";
        }

        return generatedCode;
    }
    
}
