package tile.ast.base;

public abstract class Generator {
    protected String generatedCode;
    
    public Generator() {
        generatedCode = new String();
    }

    public abstract void generate();
    
}
