package tile.ast.stmt;

import java.util.List;
import tile.ast.base.Statement;

public class FunctionDefinition implements Statement {

    public static class FuncArg {
        private String type;
        private String argId;
        private boolean is_ref;
        public FuncArg(String type, String argId, boolean is_ref) {
            this.type = type;
            this.argId = argId;
            this.is_ref = is_ref;
        }
    }

    private String funcId;
    private List<FuncArg> args;
    private Statement block;

    public FunctionDefinition(String funcId, List<FuncArg> args, Statement block) {
        this.funcId = funcId;
        this.args = args;
        this.block = block;
    }

    @Override
    public String generateTasm(String generatedCode) {
        generatedCode += "proc " + funcId + "\n";
        for (int i = 0; i < args.size(); i++) {
            generatedCode += "    ";
            generatedCode += "store " + (i) + " ;" + args.get(i).type + " " + args.get(i).argId + "\n";
        }
        generatedCode = block.generateTasm(generatedCode);
        
        // if it is referance push to stack back
        for (int i = 0; i < args.size(); i++) {
            if (args.get(i).is_ref) {
                generatedCode += "    ";
                generatedCode += "load " + (i) + "\n";
            }
        }
        generatedCode += "    ret\n";
        generatedCode += "endp\n";

        return generatedCode;
    }
    
}
