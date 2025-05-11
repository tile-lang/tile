package tile.ast.base;

public abstract class Generator {
    public String generatedCode;
    
    public Generator() {
        generatedCode = new String();
    }

    public abstract void generate();
    
}
