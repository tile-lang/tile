package tile.ast.stmt;

import tile.ast.base.Statement;
import tile.ast.types.TypeResolver.TypeInfoRetStmt;

public class ReturnStmt implements Statement {

    private Statement exprStmt;
    private TypeInfoRetStmt typeInfo;

    public ReturnStmt(Statement exprStmt, TypeInfoRetStmt typeInfo) {
        this.exprStmt = exprStmt;
        this.typeInfo = typeInfo;
    }

    @Override
    public String generateTasm(String generatedCode) {
        generatedCode = ((ExpressionStmt)exprStmt).generateTasm(generatedCode);
        if (typeInfo.expr_type.equals("int") && typeInfo.ret_type.equals("float")) {
            generatedCode += "; cast float to int\n";
            generatedCode += "    ";
            generatedCode += "ci2f\n";
            typeInfo.result_type = "float";
        }
        else if (typeInfo.expr_type.equals("float") && typeInfo.ret_type.equals("int")) {
            generatedCode += "; cast int to float\n";
            generatedCode += "    ";
            generatedCode += "cf2i\n";
            typeInfo.result_type = "int";
        }
        generatedCode += "    ret\n";
        return generatedCode;
    }
    
}
