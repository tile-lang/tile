package tile.ast.stmt;

import tile.ast.base.Expression;
import tile.ast.base.Statement;

public class IfStatement implements Statement {

    Expression condition;
    Block block;
    IfStatement altarnate;

    public IfStatement(Expression condition, Block block, IfStatement altarnate) {
        this.condition = condition;
        this.block = block;
        this.altarnate = altarnate;
    }

    @Override
    public String generateTasm(String generatedCode) {
        // TODO: implement generated tasm code for if statement
        return generatedCode;
    }

}
