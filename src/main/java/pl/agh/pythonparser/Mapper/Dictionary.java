package pl.agh.pythonparser.Mapper;

/**
 *  Klasa Dictionary zawiera często powtarzające się znaki specjalne. Dzięki użyciu konwencji, możemy w klasie Listenera,
 *  szybko znaleźć interesjący nas fragment kodu, wiedząc, jakie znaki zawiera.
 */
public class Dictionary {
    public static final String SPACE = " ";
    public static final String TAB = "\t";
    public static final String DOT = ".";
    public static final String NL = "\n";
    public static final String COMMA = ",";
    public static final String SEMICOLON = ";";
    public static final String COLON = ":";
    public static final String QUOTA = "\"";
    public static final String OPEN_BRACE = "{";
    public static final String CLOSE_BRACE = "}";
    public static final String OPEN_BRACKET = "(";
    public static final String CLOSE_BRACKET = ")";
    public static final String OPEN_SQ_BRACKET = "[";
    public static final String CLOSE_SQ_BRACKET = "]";
    public static final String COMPARE = "==";
    public static final String ASSIGN = "=";
    public static final String APO = "'";
    public static final String SLASH = "/";
    public static final String CLASS_DEF = "public class ";
    public static final String RETURN = "return ";
    public static final String MAIN_CLASS_INTRO = "public class Main {";
    public static final String MAIN_FUNCTION_INTRO = "public static void main(String[] args) {\n";
    public static final String MAIN_FUNCTION_BODY = "\t\tSource.execute();\n";
    public static final String MAIN_FUNCTION_OUTRO = "\t}\n";
    public static final String SOURCE_CLASS_INTRO = "public static class Source {\n\t\tstatic void execute() {";

}
