package tile.ast.expr;

import tile.ast.base.Expression;

public class PrimaryExpression implements Expression {

    private String value;
    private String type;
    private String unaryOp;
    private boolean isIdentifier;
    private int identifierTasmIdx;

    public PrimaryExpression(String unaryOp, String value, String type, boolean isIdentifier, int tasmIdx) {
        if (unaryOp != null) {
            this.value = unaryOp + value;
        } else {
            this.value = value;
        }
        this.type = type;
        this.isIdentifier = isIdentifier;
        this.identifierTasmIdx = tasmIdx;
        this.unaryOp = unaryOp;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String generateTasm(String generatedCode) {
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
    
}
