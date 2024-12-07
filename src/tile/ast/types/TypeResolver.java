package tile.ast.types;

import java.util.Arrays;

public class TypeResolver {
    public static class TypeInfoBinop {
        public String result_type = null;
        public String lhs_type = null;
        public boolean lhs_auto_cast = false;
        public String rhs_type = null;
        public boolean rhs_auto_cast = false;
    }

    public static class TypeInfoCast {
        public String result_type; // result type
        public String expr_type; // type of child expr
        public String cast_type; // wanted type for converting
    }

    public static class TypeInfoRetStmt {
        public String result_type; // result type
        public String expr_type; // type of child expr
        public String ret_type; // return type of function
    }

    public static class TypeFuncCall {
        public String result_type; // result type
    }

    public static String[] NumericTypes = {"int", "float"};
    
    public static boolean isNumericType(String type) {
        return Arrays.asList(NumericTypes).contains(type);
    }
    
    public static boolean isVoidType(String type) {
        return type.equals("void");
    }

    public static TypeInfoBinop resolveBinopNumericType(String lhs, String rhs) {
        // TODO: add auto cast feature
        TypeInfoBinop ti = new TypeInfoBinop();
        if (lhs.equals("float") || rhs.equals("float")) {
            ti.lhs_type = lhs;
            ti.rhs_type = rhs;
            ti.result_type = "float";
            if (lhs.equals("float") && !rhs.equals("float")) {
                ti.rhs_auto_cast = true;
            } else if (!lhs.equals("float") && rhs.equals("float")) {
                ti.lhs_auto_cast = true;
            }
        } else if (lhs.equals("int") && rhs.equals("int")) {
            ti.lhs_type = lhs;
            ti.rhs_type = rhs;
            ti.result_type = "int";
        } else {
            // err handling could be neccesarry
        }

        return ti;
    }

    public static TypeInfoCast resolveCastType(String expr, String cast) {
        TypeInfoCast ti = new TypeInfoCast();
        if (isNumericType(expr) && isNumericType(cast)) {
            ti.expr_type = expr;
            ti.cast_type = cast;
            ti.result_type = cast;
        }
        // IMPORTANT: ti can have null values be careful!
        return ti;
    }

    public static TypeInfoRetStmt resolveRetStmtType(String expr, String ret) {
        TypeInfoRetStmt tr = new TypeInfoRetStmt();
        if (isNumericType(expr) && isNumericType(ret)) {
            tr.expr_type = expr;
            tr.ret_type = ret;

            if (!(tr.expr_type.equals(tr.ret_type))) {
                System.out.println("WARNING: autocast from type '" + tr.expr_type + "' to type '" + tr.ret_type + "' may be unwanted!");
            }

            tr.result_type = ret;
        } else {
            tr.expr_type = expr;
            tr.ret_type = ret;
            if (isVoidType(expr) && isVoidType(ret)) {
                tr.result_type = ret;
            } else {
                System.err.println("ERROR: expression type '" + tr.expr_type + "' does not match with function return type '" + tr.ret_type + "' causing error!");
            }
        }
        // IMPORTANT: tr can have null values be careful!
        return tr;
    }

}
