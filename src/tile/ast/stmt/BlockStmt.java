package tile.ast.stmt;
import java.util.ArrayList;
import java.util.List;

import tile.ast.base.Statement;

public class BlockStmt implements Statement {

    private List<Statement> statements;

    public static int scopeId = 0;
    public static int ifStmtId = 0;

    public BlockStmt() {
        this.statements = new ArrayList<>();
    }

    public void addStatement(Statement stmt) {
        statements.add(stmt);
    }

    @Override
    public String generateTasm(String generatedCode) {
        scopeId++;
        for (int i = 0; i < statements.size(); i++) {
            generatedCode = statements.get(i).generateTasm(generatedCode);
        }
        scopeId--;
        ifStmtId++;
        return generatedCode;
    }
    
}
