package tile.ast.expr;

import tile.ast.base.Expression;
import tile.ast.types.TypeResolver.TypeInfoBinop;

public class MultiplicativeExpression implements Expression {

    Expression left, right;
    private TypeInfoBinop typeInfo; // type of the result;
    private String operator;

    public MultiplicativeExpression(Expression left, String operator, Expression right, TypeInfoBinop typeInfo) {
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
        if (typeInfo.lhs_auto_cast == true) {
            generatedCode += "    ci2f\n";
        }
        generatedCode = right.generateTasm(generatedCode);
        if (typeInfo.rhs_auto_cast == true) {
            generatedCode += "    ci2f\n";
        }

        // mult/div operation
        generatedCode += "    ";
        if (operator.equals("*")) {
            if (typeInfo.result_type.equals("float")) {
                generatedCode += "multf\n";
            } else if (typeInfo.result_type.equals("int")) {
                generatedCode += "mult\n";
            } else {
                System.err.println("'" + operator + "'" + " operator is only for numeric types (int, float)");
            }
        } else if (operator.equals("/")) {
            if (typeInfo.result_type.equals("float")) {
                generatedCode += "divf\n";
            } else if (typeInfo.result_type.equals("int")) {
                generatedCode += "div\n";
            } else {
                System.err.println("'" + operator + "'" + " operator is only for numeric types (int, float)");
            }
        }
        return generatedCode;
    }
    
}
