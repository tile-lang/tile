package tile.ast.expr;

import tile.ast.base.Expression;
import tile.ast.types.TypeResolver.TypeInfoLogicalBinop;

public class LogicalExpression implements Expression {
    Expression left, right;
    private TypeInfoLogicalBinop typeInfo; // type of the result;
    private String operator;

    public LogicalExpression(Expression left, String operator, Expression right, TypeInfoLogicalBinop typeInfo) {
        this.left = left;
        this.right = right;
        this.operator = operator;
        this.typeInfo = typeInfo;
    }

    @Override
    public String getType() {
        return typeInfo.result_type;
    }

    @Override
    public String generateTasm(String generatedCode) {
        // auto cast
        generatedCode = left.generateTasm(generatedCode);
        generatedCode = right.generateTasm(generatedCode);

        if (operator.equals("&&")) {
            generatedCode += "    and\n";
        }

        return generatedCode;
    }
}
