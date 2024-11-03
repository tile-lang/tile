package tile.ast.stmt;

import java.util.List;
import tile.ast.base.Statement;

public class FunctionDefinition implements Statement {

    private String funcName;
    private List<String> arg_types;
    private List<String> args;
    private Statement block;

    public FunctionDefinition(String funcName, List<String> arg_types, List<String> args, Statement block) {
        this.funcName = funcName;
        this.arg_types = arg_types;
        this.args = args;
        this.block = block;
    }

    @Override
    public String generateTasm(String generatedCode) {
        generatedCode += "proc " + funcName + "\n";
        for (int i = 0; i < args.size(); i++) {
            generatedCode += "    ";
            generatedCode += "store " + Integer.toString(i) + " ;" + arg_types.get(i) + " " + args.get(i) + "\n";
        }
        generatedCode = block.generateTasm(generatedCode);
        generatedCode += "    ret\n";
        generatedCode += "endp\n";

        return generatedCode;
    }
    
}
