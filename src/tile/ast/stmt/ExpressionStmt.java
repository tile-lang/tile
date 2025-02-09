package tile.ast.stmt;

import tile.ast.base.*;

public class ExpressionStmt implements Statement {

    private Expression expr;
    private boolean generate;

    public ExpressionStmt(Expression expr, boolean generate) {
        this.expr = expr;
        this.generate = generate;
    }

    public String getType() {
        if (expr == null) {
            return "void";
        }
        return expr.getType();
    }

    @Override
    public String generateTasm(String generatedCode) {
        if (generate == true && expr != null) {
            generatedCode = expr.generateTasm(generatedCode);
        }
        return generatedCode;
    }
    
}
