package tile.ast.stmt;
import java.util.ArrayList;
import java.util.List;

import tile.ast.base.Statement;

public class BlockStmt implements Statement {

    public static enum BlockType {
        Regular,
        IfBlock,
        FuncDefBlock,
        WhileLoopBlock,
        ForLoopBlock
    }

    private BlockType blockType;
    private List<Statement> statements;

    public static int scopeId = 0;
    public static int ifStmtId = 0;

    public BlockStmt(BlockType blockType) {
        this.blockType = blockType;
        this.statements = new ArrayList<>();
    }

    public void addStatement(Statement stmt) {
        statements.add(stmt);
    }

    public BlockType getBlockType() {
        return blockType;
    }

    @Override
    public String generateTasm(String generatedCode) {
        scopeId++;
        for (int i = 0; i < statements.size(); i++) {
            generatedCode = statements.get(i).generateTasm(generatedCode);
        }
        scopeId--;
        if (this.blockType == BlockType.IfBlock) {
            ifStmtId++;
        }
        return generatedCode;
    }
    
}
