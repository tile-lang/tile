package tile.ast.stmt;

import java.util.List;

import tile.ast.base.Statement;

public class TypeDefinition implements Statement {
    public enum Kind {
        STRUCT,
        UNION
    }

    public String typeName;
    public Kind kind;

    public static class Field {
        public String id;
        public String type;
        public int type_size;

        public Field(String id, String type, int type_size) {
            this.id = id;
            this.type = type;
            this.type_size = type_size;
        }
    }

    private List<Field> fields;     // for struct
    private List<String> variants;  // for union

    public TypeDefinition(String typeName, Kind kind, List<Field> fields) {
        this.typeName = typeName;
        this.kind = kind;
        this.fields = fields;
    }

    @Override
    public String generateTasm(String generatedCode) {
        /*
        This method Does Not Work like the others
         */
        return generatedCode;
    }

    public List<Field> getFields() {
        return fields;
    }
}
