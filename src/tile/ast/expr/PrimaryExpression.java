package tile.ast.expr;

import tile.ast.base.Expression;

public class PrimaryExpression implements Expression {

    private String value;

    public PrimaryExpression(String value) {
        this.value = value;
    }

    @Override
    public String generateTasm(String generatedCode) {
        generatedCode += "    ";
        generatedCode += "push " + value + "\n";
        return generatedCode;
    }
    
}
