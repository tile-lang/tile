package tile.ast.stmt;

import tile.ast.base.Statement;

public class ReturnStmt implements Statement {

    private Statement exprStmt;

    public ReturnStmt(Statement exprStmt) {
        this.exprStmt = exprStmt;
    }

    @Override
    public String generateTasm(String generatedCode) {
        generatedCode = ((ExpressionStmt)exprStmt).generateTasm(generatedCode);
        return generatedCode;
    }
    
}
