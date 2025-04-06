package tile.ast.expr;

import tile.ast.base.Expression;
import tile.ast.types.TypeResolver.TypeInfoBinopInt;

public class ShiftExpression implements Expression {
    Expression left, right;
    private TypeInfoBinopInt typeInfo;
    private String operator;

    public ShiftExpression(Expression left, String operator, Expression right, TypeInfoBinopInt typeInfo) {
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
        generatedCode = left.generateTasm(generatedCode);
        generatedCode = right.generateTasm(generatedCode);

        if (operator.equals("<<")) {
            generatedCode += "    shl\n";
        } else if (operator.equals(">>")) {
            generatedCode += "    shr\n";
        } else {
            System.err.println("Unknown shift operator: " + operator);
        }

        return generatedCode;
    }
}
