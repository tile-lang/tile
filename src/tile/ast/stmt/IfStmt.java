package tile.ast.stmt;

import tile.ast.base.Expression;
import tile.ast.base.Statement;

public class IfStmt implements Statement {

    private Expression condition;
    private Statement block;
    private Statement altarnate;

    public IfStmt(Expression condition, Statement block, Statement altarnate) {
        this.condition = condition;
        this.block = block;
        this.altarnate = altarnate;
    }

    public String generateIf(String generatedCode, int ifStmtId, int ifId, int scopeId) {
        generatedCode = condition.generateTasm(generatedCode);
        
        generatedCode += "    ";
        generatedCode += "jz " + "_" + scopeId + "_" + ifStmtId + "else" + ifId + "\n";

        if (block != null) {
            generatedCode = block.generateTasm(generatedCode);
            generatedCode += "    ";
            generatedCode += "jmp " + "_" + scopeId + "exit" + ifStmtId + "\n";
        }

        generatedCode += "_" + scopeId + "_" + ifStmtId + "else" + ifId + ": \n";
        if (altarnate != null) {
            if (altarnate instanceof tile.ast.stmt.IfStmt) {
                generatedCode = ((IfStmt)altarnate).generateIf(generatedCode, ifStmtId, ifId + 1, scopeId);
            } else {
                generatedCode = altarnate.generateTasm(generatedCode);
            }
        }

        ifId++;

        return generatedCode;
    }

    @Override
    public String generateTasm(String generatedCode) {
        
        int scopeId = BlockStmt.scopeId;
        int ifStmtId = BlockStmt.ifStmtId;
        generatedCode += "; if begin\n";
        generatedCode = generateIf(generatedCode, ifStmtId, 0, scopeId);
        generatedCode += "_" + scopeId + "exit" + ifStmtId + ": \n";
        generatedCode += "; if end\n";

        return generatedCode;
    }

}
