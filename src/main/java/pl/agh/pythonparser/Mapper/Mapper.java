package pl.agh.pythonparser.Mapper;

import org.antlr.v4.runtime.tree.ParseTree;
import pl.agh.pythonparser.Python3Parser;

import java.util.ArrayList;
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

    public static boolean isComplexStatement(ParseTree node) {
        if (node.getChildCount() < 1) {
            return false;
        } else if (node.getChildCount() == 1) {
            return isComplexStatement(node.getChild(0));
        } else {
            return true;
        }
    }

    public static boolean isLeaf(ParseTree node) {
        return !isComplexStatement(node);
    }

    public static boolean hasExtendedStatement(ParseTree node, Class<? extends ParseTree> pattern) {
       if (pattern.isInstance(node) && (node.getChildCount() > 1)) {
           return true;
       }

       if (node.getChildCount() == 0) {
           return false;
       } else if (node.getChildCount() == 1) {
            return hasExtendedStatement(node.getChild(0), pattern);
       } else if (node.getChildCount() > 1) {
           for (int i = 0; i < node.getChildCount(); i++) {
               if (hasExtendedStatement(node.getChild(i), pattern)) {
                   return true;
               }
           }
       }

       return false;
    }

    public static boolean hasParentType(ParseTree node, Class<? extends ParseTree> pattern) {
        if (pattern.isInstance(node)) {
            return true;
        }

        if (node.getParent() instanceof Python3Parser.File_inputContext) {
            return false;
        }

        return hasParentType(node.getParent(), pattern);
    }


    public static boolean hasComplexParentType (ParseTree node, Class<? extends ParseTree> pattern) {
        if (pattern.isInstance(node) && (node.getChildCount() > 1)) {
            return true;
        }

        if (node.getParent() instanceof Python3Parser.File_inputContext) {
            return false;
        }

        return hasParentType(node.getParent(), pattern);
    }

    public static ParseTree getExtendedChild (ParseTree node, Class<? extends ParseTree> pattern) {
        if (pattern.isInstance(node) && (node.getChildCount() > 1)) {
            return node;
        }

        if (node.getChildCount() == 0) {
            return null;
        } else if (node.getChildCount() == 1) {
            return getExtendedChild(node.getChild(0), pattern);
        } else if (node.getChildCount() > 1) {
            for (int i = 0; i < node.getChildCount(); i++) {
                if (getExtendedChild(node.getChild(i), pattern) != null) {
                    return getExtendedChild(node.getChild(i), pattern);
                }
            }
        }

        return null;
    }

    public static ParseTree getLeaf (ParseTree node) {
        if (node.getChildCount() == 0) {
            return node;
        } else if (node.getChild(0).getChildCount() == 0) {
            return getLeaf(node.getChild(1));
        }

        return getLeaf(node.getChild(0));
    }


    /**
     * Drugie dziecko to zwracane wyrazenie
     * @param node
     * @return
     */
    public static String getFunctionType (ParseTree node, ArrayList<String> vars) {
        boolean hasArithmetic = hasExtendedStatement(node.getChild(1), Python3Parser.Arith_exprContext.class);
        boolean hasComparision = hasExtendedStatement(node.getChild(1), Python3Parser.ComparisonContext.class);
        boolean isVariable = vars.contains(node.getChild(1).getText());
        boolean isString = node.getChild(1).getText().startsWith("\"") || node.getChild(1).getText().startsWith("'");

        if (hasArithmetic && hasComparision) {
            return "boolean";
        } else if (hasArithmetic && isVariable) {
            return "Object";
        } else if (isString) {
            return "String";
        } else if (hasArithmetic) {
            return "Double";
        }

        return "void";
    }

    public static ParseTree getComplexParent (ParseTree node, Class<? extends ParseTree> pattern) {
        if (pattern.isInstance(node) && (node.getChildCount() > 1)) {
            return node;
        }

        if (node instanceof Python3Parser.File_inputContext) {
            return null;
        }

        return getComplexParent(node.getParent(), pattern);
    }


    public static ParseTree getFirstComplexParentForVar(ParseTree node) {
        if (node.getChildCount() > 2) {
            if (node.getChild(1).getText().equals(Dictionary.ASSIGN)) {
                return node;
            }

        }

        if (node instanceof Python3Parser.File_inputContext) {
            return null;
        }

        return getFirstComplexParentForVar(node.getParent());
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
