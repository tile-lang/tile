package tile.ast.stmt;

import tile.ast.base.Expression;
import tile.ast.base.Statement;

public class IfStmt implements Statement {

    private Expression condition;
    private Statement block;
    private Statement altarnate;

    private static int ifId = 0;

    public IfStmt(Expression condition, Statement block, Statement altarnate) {
        this.condition = condition;
        this.block = block;
        this.altarnate = altarnate;

        System.out.println("cnd: " + condition);
        System.out.println("block: " + block);
        System.out.println("altarnate: " + altarnate);
    }

    @Override
    public String generateTasm(String generatedCode) {
        //FIXME: refactor needed!
        generatedCode = condition.generateTasm(generatedCode);
        generatedCode += "jz " + "_else" + ifId + "\n";
        
        if (block != null)
            generatedCode = block.generateTasm(generatedCode);
        
        if (altarnate == null)
            generatedCode += "jmp " + "_exit" + ifId + "\n";
        
        generatedCode += "_else" + ifId + ": \n";

        int tmpId = ifId;
        ifId++;

        if (altarnate != null) {
            generatedCode = altarnate.generateTasm(generatedCode);
        }

        generatedCode += "_exit" + tmpId + ": \n";

        return generatedCode;
    }

}
