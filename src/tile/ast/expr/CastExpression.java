package tile.ast.expr;

import tile.ast.base.Expression;
import tile.ast.types.TypeResolver.TypeInfoCast;

public class CastExpression implements Expression {

    private TypeInfoCast typeInfo;
    private Expression expr; // the value

    public CastExpression(Expression expr, TypeInfoCast typeInfo) {
        this.typeInfo = typeInfo;
        this.expr = expr;
    }

    @Override
    public String getType() {
        return typeInfo.result_type;
    }

    @Override
    public String generateTasm(String generatedCode) {
        generatedCode = expr.generateTasm(generatedCode);
        generatedCode += "    ";
        if (typeInfo.expr_type.equals("int") && typeInfo.cast_type.equals("float")) {
            generatedCode += "; cast float to int\n";
            generatedCode += "    ";
            generatedCode += "ci2f\n";
            typeInfo.result_type = "float";
        }
        else if (typeInfo.expr_type.equals("float") && typeInfo.cast_type.equals("int")) {
            generatedCode += "; cast int to float\n";
            generatedCode += "    ";
            generatedCode += "cf2i\n";
            typeInfo.result_type = "int";
        }
        return generatedCode;
    }    
}
