package tile.ast.expr;

import tile.ast.base.Expression;
import tile.ast.types.TypeResolver.TypeInfoBinopBool;

public class RelationalExpression implements Expression {

    Expression left, right;
    private TypeInfoBinopBool typeInfo; // type of the result;
    private String operator;

    public RelationalExpression(Expression left, String operator, Expression right, TypeInfoBinopBool typeInfo) {
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
        if (typeInfo.type.lhs_auto_cast == true) {
            generatedCode += "    ci2f\n";
        }
        generatedCode = right.generateTasm(generatedCode);
        if (typeInfo.type.rhs_auto_cast == true) {
            generatedCode += "    ci2f\n";
        }

        generatedCode += "    ";
        if (operator.equals("<")) {
            if (typeInfo.type.result_type.equals("float")) {
                generatedCode += "ltf\n";
            } else if (typeInfo.type.result_type.equals("int")) {
                generatedCode += "lt\n";
            } else {
                System.err.println("'" + operator + "'" + " operator is only for numeric types (int, float)");
            }
        } else if (operator.equals(">")) {
            if (typeInfo.type.result_type.equals("float")) {
                generatedCode += "gtf\n";
            } else if (typeInfo.type.result_type.equals("int")) {
                generatedCode += "gt\n";
            } else {
                System.err.println("'" + operator + "'" + " operator is only for numeric types (int, float)");
            }
        } else if (operator.equals("<=")) {
            if (typeInfo.type.result_type.equals("float")) {
                generatedCode += "lef\n";
            } else if (typeInfo.type.result_type.equals("int")) {
                generatedCode += "le\n";
            } else {
                System.err.println("'" + operator + "'" + " operator is only for numeric types (int, float)");
            }
        } else if (operator.equals(">=")) {
            if (typeInfo.type.result_type.equals("float")) {
                generatedCode += "gef\n";
            } else if (typeInfo.type.result_type.equals("int")) {
                generatedCode += "ge\n";
            } else {
                System.err.println("'" + operator + "'" + " operator is only for numeric types (int, float)");
            }
        }

        return generatedCode;
    }
    
}
