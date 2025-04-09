package tile.ast.stmt;

import tile.ast.base.Expression;
import tile.ast.base.Statement;

public class ForStmt implements Statement {

    private Statement init;
    private Statement conditionStmt;
    private Expression update;
    private Statement block;

    public ForStmt(Statement init, Statement conditionStmt, Expression update, Statement block) {
        this.init = init;
        this.conditionStmt = conditionStmt;
        this.update = update;
        this.block = block;
    }

    public void setBody(Statement body) {
        this.block = body;
    }

    public void setUpdate(Expression update) {
        this.update = update;
    }

    public void setCondition(Statement condition) {
        this.conditionStmt = condition;
    }

    public Statement getInit() {
        return init;
    }

    public void setInit(Statement init) {
        this.init = init;
    }

    @Override
    public String generateTasm(String generatedCode) {
        int forId = BlockStmt.forStmtId;

        generatedCode = init.generateTasm(generatedCode);

        generatedCode = conditionStmt.generateTasm(generatedCode);
        generatedCode += "jz " + "_" + BlockStmt.scopeId + "for_end" + forId + "\n";
        generatedCode += "_" + BlockStmt.scopeId + "for" + forId + ":\n";

        generatedCode = block.generateTasm(generatedCode);
        generatedCode = conditionStmt.generateTasm(generatedCode);

        if (update != null) {
            generatedCode = update.generateTasm(generatedCode);
        }

        generatedCode += "jnz " + "_" + BlockStmt.scopeId + "for" + forId + "\n";
        generatedCode += "_" + BlockStmt.scopeId + "for_end" + forId + ":\n";

        return generatedCode;
    }
}
