package pl.agh.pythonparser.Mapper;

import java.util.HashMap;
import java.util.Map;

public class Mapper {
    private static Map<String, String> types;
    private static Map<String, String> operators;
    private static Map<String, String> buildIn;

    static {
        types = new HashMap<>();
        types.put("bool", "boolean");
        types.put("list", "List");
        types.put("float", "float");
        types.put("int", "int");
        types.put("boolean", "boolean");
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

    static {
        buildIn = new HashMap<>();
        buildIn.put("def", "function");
        buildIn.put("print", "System.out.println");
        buildIn.put("all", "Iterables.all");
        buildIn.put("any", "Iterables.any");
    }

    public static String getType(String type) {
        return !types.containsKey(type) ? type : types.get(type.toLowerCase());
    }

    public static String getOperator(String operator) {
        return !operators.containsKey(operator) ? operator : operators.get(operator.toLowerCase());
    }

    public static String getBuildIn(String buildInFoo) {
        return !buildIn.containsKey(buildInFoo) ? buildInFoo : buildIn.get(buildInFoo.toLowerCase());
    }
}
