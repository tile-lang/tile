package tile.ast.expr;

import tile.ast.base.Expression;
import tile.ast.types.TypeResolver.TypeInfoBinopBool;

public class EqualityExpression implements Expression {
    Expression left, right;
    private TypeInfoBinopBool typeInfo; // type of the result;
    private String operator;

    public EqualityExpression(Expression left, String operator, Expression right, TypeInfoBinopBool typeInfo) {
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

        if (operator.equals("==")) {
            if (typeInfo.type.result_type.equals("float")) {
                generatedCode += "    eqf\n";
            } else if (typeInfo.type.result_type.equals("int")) {
                generatedCode += "    eq\n";
            } else {
                if (typeInfo.type.result_type.equals("bool")) {
                    generatedCode += "    eq\n";
                } else if (typeInfo.type.result_type.equals("char")) {
                    generatedCode += "    eq\n";
                } else {
                    System.err.println("'" + operator + "'" + " operator is only for certain types (int, float, char, bool)");
                }
            }
        } else if (operator.equals("!=")) {
            if (typeInfo.type.result_type.equals("float")) {
                generatedCode += "    eqf\n";
                generatedCode += "    not\n";
            } else if (typeInfo.type.result_type.equals("int")) {
                generatedCode += "    eq\n";
                generatedCode += "    not\n";
            } else {
                if (typeInfo.type.result_type.equals("bool")) {
                    generatedCode += "    eq\n";
                    generatedCode += "    not\n";
                }  else if (typeInfo.type.result_type.equals("char")) {
                    generatedCode += "    eq\n";
                    generatedCode += "    not\n";
                } else {
                    System.err.println("'" + operator + "'" + " operator is only for certain types (int, float, char, bool)");
                }
            }
        }

        return generatedCode;
    }
}
