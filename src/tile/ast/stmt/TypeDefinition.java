package tile.ast.stmt;

import java.util.List;

import tile.ast.base.Statement;

public class TypeDefinition implements Statement {
    public enum Kind {
        STRUCT,
        UNION
    }

    public String name;
    public Kind kind;

    public static class Field {
        public String name;
        public String type;

        public Field(String name, String type) {
            this.name = name;
            this.type = type;
        }
    }

    public List<Field> fields;       // for struct
    public List<String> variants;    // for union

    public TypeDefinition(String name, Kind kind) {
        this.name = name;
        this.kind = kind;
    }

    @Override
    public String generateTasm(String generatedCode) {
        /*
        This method Does Not Work like the others
         */
        return generatedCode;
    }
}
