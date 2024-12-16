package tile.ast.stmt;

import tile.ast.base.Expression;
import tile.ast.base.Statement;

public class WhileStmt implements Statement {

    private Expression condition;
    private Statement block;

    /*
    while (5 < 3) {
    ...
    }

    push 5
    push 3
    lt
    jz while_end
    while:
        ; do shit
        push 5
        push 3
        lt
        jnz while
    while_end:
     */
    /*
        
     */

    public WhileStmt(Expression condition, Statement block) {
        this.condition = condition;
        this.block = block;
    }

    @Override
    public String generateTasm(String generatedCode) {
        generatedCode = condition.generateTasm(generatedCode);
        generatedCode += "jz " + "_while_end" + "\n";
        generatedCode += "_while:\n";

        generatedCode = block.generateTasm(generatedCode);

        generatedCode = condition.generateTasm(generatedCode);
        generatedCode += "jnz " + "_while" + "\n";

        generatedCode += "_while_end:\n";

        return generatedCode;
    }
    
}
