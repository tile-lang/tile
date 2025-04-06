package tile.ast.types;

import java.util.List;

public class TypeDef {
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

    public TypeDef(String name, Kind kind) {
        this.name = name;
        this.kind = kind;
    }
}
