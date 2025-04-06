package tile.ast.stmt;

import tile.ast.base.Expression;
import tile.ast.base.Statement;

public class ForStmt implements Statement {

    private Statement init;
    private Expression condition;
    private Statement update;
    private Statement block;

    public ForStmt(Statement init, Expression condition, Statement update, Statement block) {
        this.init = init;
        this.condition = condition;
        this.update = update;
        this.block = block;
    }

    @Override
    public String generateTasm(String generatedCode) {
        int forId = BlockStmt.forStmtId;

        if (init != null) {
            generatedCode = init.generateTasm(generatedCode);
        }

        generatedCode += "_" + BlockStmt.scopeId + "for" + forId + ":\n";

        if (condition != null) {
            generatedCode = condition.generateTasm(generatedCode);
        }

        generatedCode += "jz _" + BlockStmt.scopeId + "for_end" + forId + "\n";
        generatedCode = block.generateTasm(generatedCode);

        generatedCode += "_" + BlockStmt.scopeId + "for_update" + forId + ":\n";
        if (update != null) {
            generatedCode = update.generateTasm(generatedCode);
        }

        generatedCode += "jnz _" + BlockStmt.scopeId + "for" + forId + "\n";
        generatedCode += "_" + BlockStmt.scopeId + "for_end" + forId + ":\n";

        return generatedCode;
    }
}
