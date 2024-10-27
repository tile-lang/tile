package tile;

import java.util.ArrayList;
import java.util.List;
import tile.ast.base.Statement;
import tile.ast.base.Generator;

public class Program extends Generator {
    private List<Statement> statements;

    public Program() {
        super();
        statements = new ArrayList<Statement>();
    }

    public void addStatement(Statement stmt) {
        statements.add(stmt);
    }

    @Override
    public void generate() {
        // TODO: generate tasm code entry point.
    }
    
}
