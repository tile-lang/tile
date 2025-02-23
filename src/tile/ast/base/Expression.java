package tile.ast.base;

public interface Expression {
    public String generateTasm(String generatedCode);
    public String getType();
}
