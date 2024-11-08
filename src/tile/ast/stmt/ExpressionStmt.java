package tile.ast.stmt;

import tile.ast.base.*;

public class ExpressionStmt implements Statement {

    private Expression expr;

    public ExpressionStmt(Expression expr) {
        this.expr = expr;
    }

    @Override
    public String generateTasm(String generatedCode) {
        return expr.generateTasm(generatedCode);
    }
    
}
