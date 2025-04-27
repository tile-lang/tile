package tile.ast.stmt;

public interface Variable {
    public void setTasmIdx(int idx);

    public int getTasmIdx();

    public String getType();

    public String getVarId();

    public void setAsGlobal();
}
