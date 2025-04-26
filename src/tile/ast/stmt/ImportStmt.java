package tile.ast.stmt;

import tile.Program;
import tile.ast.base.Statement;

public class ImportStmt implements Statement {

    private final String importPath;
    private final Program program;

    public ImportStmt(String importPath, Program program) {
        this.importPath = importPath;
        this.program = program;
    }

    public String getPath() {
        return importPath;
    }

    @Override
    public String generateTasm(String generatedCode) {
        String modifiedPath = "; import " + importPath.replace("\\", "/") + "\n";
        generatedCode += modifiedPath;
        program.generate();
        generatedCode += program.generatedCode;
        return generatedCode;
    }
}
