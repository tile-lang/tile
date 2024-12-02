package tile.ast.expr;

import tile.ast.base.Expression;

public class PrimaryExpression implements Expression {

    private String value;
    private String type;

    public PrimaryExpression(String value, String type) {
        this.value = value;
        this.type = type;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String generateTasm(String generatedCode) {
        generatedCode += "    ";
        generatedCode += "push " + value + "\n";
        return generatedCode;
    }
    
}
