package tile.ast.stmt;

import tile.ast.base.Statement;

import java.util.stream.Collectors;
import java.util.List;

public class TasmStmt implements Statement {

    private final List<String> tasmLines;

    public TasmStmt(List<String> tasmLines) {
        this.tasmLines = tasmLines;
    }

    public List<String> getTasmLines() {
        return tasmLines;
    }

    @Override
    public String generateTasm(String generatedCode) {
        List<String> modifiedList = tasmLines.stream()
                .map(s -> "    " + s.replace("\"", "") + " ; unsafe tasm statement\n")
                .collect(Collectors.toList());
        for (int i = 0; i < modifiedList.size(); i++) {
            generatedCode += modifiedList.get(i);
        }
        return generatedCode;
    }
}
