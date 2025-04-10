package tile.ast.expr;

import java.sql.PreparedStatement;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;

import tile.PrePassStatement;
import tile.ast.base.Expression;
import tile.ast.types.TypeResolver;

public class PrimaryExpression implements Expression {

    private String value;
    private String type;
    private String unaryOp;
    private boolean isIdentifier;
    private int identifierTasmIdx;
    private int dataTasmIdx;

    public PrimaryExpression(String unaryOp, String value, String type, boolean isIdentifier, int tasmIdx, int dataTasmIdx) {
        if (unaryOp != null) {
            if (unaryOp.equals("-") || unaryOp.equals("+")) {
                this.value = unaryOp + value;
            } else {
                this.value = value;
            }
        } else {
            this.value = value;
        }
        this.type = type;
        this.isIdentifier = isIdentifier;
        this.identifierTasmIdx = tasmIdx;
        this.dataTasmIdx = dataTasmIdx;
        this.unaryOp = unaryOp;
    }

    @Override
    public String getType() {
        return type;
    }

    private String generateTasmForPrimitive(String generatedCode) {
        generatedCode += "    ";
        if (isIdentifier) {
            boolean isUnaryNotNull = unaryOp != null;
            if (isUnaryNotNull) {
                if (unaryOp.equals("-")) {
                    if (type.equals("int")) {
                        generatedCode += "    ";
                        generatedCode += "push 0" + "\n";
                    } else if (type.equals("float")) {
                        generatedCode += "    ";
                        generatedCode += "push 0.0" + "\n";
                    }
                }
            }
            generatedCode += "load " + identifierTasmIdx + "\n";
            if (isUnaryNotNull) {
                if (unaryOp.equals("-")) {
                    if (type.equals("int")) {
                        generatedCode += "    ";
                        generatedCode += "sub" + "\n";
                    } else if (type.equals("float")) {
                        generatedCode += "    ";
                        generatedCode += "subf" + "\n";
                    }
                }
            }

            if (isUnaryNotNull) {
                // TODO: solve the "load"ing the value problem for each iteration unary operator ++ or -- called. It fills stack! (use pop or don't generate the load below under some conditions!)
                if (unaryOp.equals("--")) {
                    if (type.equals("int")) {
                        generatedCode += "    dec" + " ; --\n";
                    } else if (type.equals("float")) {
                        generatedCode += "    decf" + " ; --\n";
                    }
                    generatedCode += "    store " + identifierTasmIdx + "\n";
                    generatedCode += "    load " + identifierTasmIdx + "\n";
                } else if (unaryOp.equals("++")) {
                    if (type.equals("int")) {
                        generatedCode += "    inc" + " ; ++\n";
                    } else if (type.equals("float")) {
                        generatedCode += "    incf" + " ; ++\n";
                    }
                    generatedCode += "    store " + identifierTasmIdx + "\n";
                    generatedCode += "    load " + identifierTasmIdx + "\n";
                }
            }
        } else {
            generatedCode += "push " + value + "\n";
        }
        return generatedCode;
    }

    private <T, E> T getKeyByValue(Map<T, E> map, E value) {
        for (Entry<T, E> entry : map.entrySet()) {
            if (Objects.equals(value, entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

    public String generateTasmForString(String generatedCode) {
        
        String str = getKeyByValue(PrePassStatement.globalDataTableIndices, dataTasmIdx);

        generatedCode += "    aloadc " + dataTasmIdx * 2 + " ; " + str + "\n";
        return generatedCode;
    }

    @Override
    public String generateTasm(String generatedCode) {
        System.out.println("primary type:" + type);
        if (TypeResolver.isNumericType(type) || TypeResolver.isCharType(type) || TypeResolver.isBoolType(type)) {
            generatedCode = generateTasmForPrimitive(generatedCode);
        } else if (TypeResolver.isStringType(type)) {
            generatedCode = generateTasmForString(generatedCode);
        } else {
            generatedCode += "    load " + identifierTasmIdx + "\n";
        }

        return generatedCode;
    }
    
}
