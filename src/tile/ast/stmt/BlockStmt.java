package tile.ast.stmt;
import java.util.ArrayList;
import java.util.List;

import tile.ast.base.Statement;

public class BlockStmt implements Statement {

    private List<Statement> statements;

    public BlockStmt() {
        this.statements = new ArrayList<>();
    }

    public void addStatement(Statement stmt) {
        statements.add(stmt);
    }

    @Override
    public String generateTasm(String generatedCode) {
        for (int i = 0; i < statements.size(); i++) {
            generatedCode = statements.get(i).generateTasm(generatedCode);
        }
        return generatedCode;
    }
    
}
