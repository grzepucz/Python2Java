package pl.agh.pythonparser.Mapper;

import java.util.HashMap;
import java.util.Map;

public class Mapper {
    private static Map<String, String> types;
    private static Map<String, String> operators;

    static {
        types = new HashMap<>();
        types.put("boolean", "int");
        types.put("char", "char");
        types.put("word", "int");
        types.put("integer", "int");
        types.put("longint", "long");
        types.put("real", "float");
        types.put("string", "char*");
        types.put("def", "function");
    }

    static {
        operators = new HashMap<>();
        operators.put("nil", "NULL");
        operators.put("or", "||");
        operators.put("and", "&&");
        operators.put(":=", "=");
        operators.put("=", "==");
        operators.put("<>", "!=");
        operators.put("<=", "<=");
        operators.put(">=", ">=");
        operators.put("<", "<");
        operators.put(">", ">");
        operators.put("+", "+");
        operators.put("-", "-");
        operators.put("*", "*");
        operators.put("/", "/");
        operators.put("div", "/");
        operators.put("def", "function");
        operators.put("mod", "%");
    }

    public static String getType(String type) {
        return types.get(type.toLowerCase());
    }

    public static String getOperator(String operator) {
        return operators.get(operator.toLowerCase());
    }
}
