package tile.ast.expr;

import tile.ast.base.Expression;

public class PrimaryExpression implements Expression {

    private String value;
    private String type;
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
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String generateTasm(String generatedCode) {
        generatedCode += "    ";
        if (isIdentifier) {
            generatedCode += "load " + identifierTasmIdx + "\n";
        } else {
            generatedCode += "push " + value + "\n";
        }
        return generatedCode;
    }
    
}
