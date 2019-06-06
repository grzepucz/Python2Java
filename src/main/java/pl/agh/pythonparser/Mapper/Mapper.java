package pl.agh.pythonparser.Mapper;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

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
        buildIn.put("print", "System.out.println");
        buildIn.put("len", "size");
        buildIn.put("range", "Range.between");
        buildIn.put("all", "Iterables.all");
        buildIn.put("any", "Iterables.any");
        buildIn.put("self", "this");
        buildIn.put("__doc__", "doc");
    }

    public static boolean isSpecial(String context) {
        return Mapper.getOperator(context) != null
                || Mapper.getType(context) != null
                || Mapper.getBuildIn(context) != null
                || Pattern.compile("^[a-z]+\\(.*\\)$").matcher(context).matches();
    }

    public static String getSpecial(String context) {
        if (Mapper.getOperator(context) != null)
        {
            return Mapper.getOperator(context);
        }
        else if (Mapper.getType(context) != null)
        {
            return Mapper.getType(context);
        }
        else if (Mapper.getBuildIn(context) != null)
        {
            return Mapper.getBuildIn(context);
        } else
            return "";
    }

    public static String getType(String type) {
        return !types.containsKey(type) ? null : types.get(type.toLowerCase());
    }

    public static String getOperator(String operator) {
        return !operators.containsKey(operator) ? null : operators.get(operator.toLowerCase());
    }

    public static String getBuildIn(String buildInFoo) {
        return !buildIn.containsKey(buildInFoo) ? null : buildIn.get(buildInFoo.toLowerCase());
    }
}
