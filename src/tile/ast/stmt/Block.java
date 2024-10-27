package tile.ast.stmt;
import java.util.List;

import tile.ast.base.Statement;

public class Block implements Statement {

    private List<Statement> statements;

    public void addStatement(Statement stmt) {
        statements.add(stmt);
    }

    @Override
    public String generateTasm(String generatedCode) {
        // TODO: implement generated tasm code for block statement
        return generatedCode;
    }
    
}
