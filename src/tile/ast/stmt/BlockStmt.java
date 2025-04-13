package tile.ast.stmt;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tile.ast.base.Statement;

public class BlockStmt implements Statement {

    public static enum BlockType {
        Regular,
        IfBlock,
        FuncDefBlock,
        WhileLoopBlock,
        ForLoopBlock
    }

    public Map<String, Variable> variableSymbols;

    private BlockType blockType;
    public List<Statement> statements;

    private int blockId = 0;
    public static int scopeId = 0;
    public static int generalBlockId = 0; // it is for defining vars!
    public static int ifStmtId = 0;
    public static int whileStmtId = 0;
    public static int forStmtId = 0;

    public BlockStmt(BlockType blockType) {
        this.blockType = blockType;
        this.statements = new ArrayList<>();
        this.variableSymbols = new HashMap<>();
        blockId = generalBlockId++;
    }

    public void addStatement(Statement stmt) {
        statements.add(stmt);
    }

    public BlockType getBlockType() {
        return blockType;
    }

    public int getBlockId() {
        return blockId;
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
        } else if (this.blockType == BlockType.WhileLoopBlock) {
            whileStmtId++;
        } else if (this.blockType == BlockType.ForLoopBlock) {
            forStmtId++;
        }
        return generatedCode;
    }
    
}
