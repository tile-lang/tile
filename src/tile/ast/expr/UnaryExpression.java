package tile.ast.expr;

import tile.ast.base.Expression;

public class UnaryExpression implements Expression {

    private String operator;
    private Expression innerExpr;

    public UnaryExpression(String unaryOp, Expression innerExpr) {
        this.operator = unaryOp;
        this.innerExpr = innerExpr;
    }

    @Override
    public String getType() {
        return innerExpr.getType();
    }

    @Override
    public String generateTasm(String generatedCode) {
        if (operator.equals("-")) {
            if (getType().equals("int")) {
                generatedCode += "    push -1\n";
            } else if (getType().equals("float")) {
                generatedCode += "    push -1.0\n";
            }
        }
        generatedCode = innerExpr.generateTasm(generatedCode);
        if (operator.equals("-")) {
            if (getType().equals("int")) {
                generatedCode += "    mult\n";
            } else if (getType().equals("float")) {
                generatedCode += "    multf\n";
            }
        }
        return generatedCode;
    }
    
}
