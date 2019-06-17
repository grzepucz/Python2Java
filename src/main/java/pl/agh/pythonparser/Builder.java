package pl.agh.pythonparser;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.List;

public final class Builder {

    private static final DescriptiveBailErrorListener ERROR_LISTENER = new DescriptiveBailErrorListener();

    // No need to instantiate this class.
    private Builder() {
    }

    public static final class Lexer {

        private Python3Lexer lexer;

        public Lexer(String input) {
            this(CharStreams.fromString(input));
        }

        public Lexer(CharStream input) {
            this.lexer = new Python3Lexer(input);
            this.lexer.removeErrorListeners();
            this.lexer.addErrorListener(ERROR_LISTENER);
        }

        public Lexer withErrorListener(ANTLRErrorListener listener) {
            this.lexer.removeErrorListeners();
            this.lexer.addErrorListener(listener);
            return this;
        }

        public Python3Lexer build() {
            return this.lexer;
        }
    }

    public static final class Parser {

        private Python3Parser parser;

        public Parser(String input) {
            this(CharStreams.fromString(input));
        }

        public Parser(CharStream input) {
            Python3Lexer lexer = new Python3Lexer(input);
            lexer.removeErrorListeners();
            lexer.addErrorListener(ERROR_LISTENER);
            this.parser = new Python3Parser(new CommonTokenStream(lexer));
            this.parser.removeErrorListeners();
            this.parser.addErrorListener(ERROR_LISTENER);
        }

        public Parser(Python3Lexer lexer) {
            this.parser = new Python3Parser(new CommonTokenStream(lexer));
            this.parser.removeErrorListeners();
            this.parser.addErrorListener(ERROR_LISTENER);
        }

        public Parser withErrorListener(ANTLRErrorListener listener) {
            this.parser.removeErrorListeners();
            this.parser.addErrorListener(listener);
            return this;
        }

        public Python3Parser build() {
            return this.parser;
        }
    }

    public static final class Tree {

        private final String input;

        public Tree(String input) {
            this.input = input;
        }

        public String toStringASCII() {

            Python3Parser parser = new Builder.Parser(input).build();
            ParseTree tree = parser.file_input();

            StringBuilder builder = new StringBuilder();

            walk(tree, builder);

            return builder.toString();
        }

        @SuppressWarnings("unchecked")
        private void walk(ParseTree tree, StringBuilder builder) {

            List<ParseTree> firstStack = new ArrayList<>();
            firstStack.add(tree);

            List<List<ParseTree>> childListStack = new ArrayList<>();
            childListStack.add(firstStack);

            while (!childListStack.isEmpty()) {

                List<ParseTree> childStack = childListStack.get(childListStack.size() - 1);

                if (childStack.isEmpty()) {
                    childListStack.remove(childListStack.size() - 1);
                }
                else {
                    tree = childStack.remove(0);

                    String node = tree.getClass().getSimpleName().replace("Context", "");
                    node = Character.toLowerCase(node.charAt(0)) + node.substring(1);

                    String indent = "";

                    for (int i = 0; i < childListStack.size() - 1; i++) {
                        indent += (childListStack.get(i).size() > 0) ? "|  " : "   ";
                    }

                    String text;

                    if (tree instanceof TerminalNode && tree.getText().trim().isEmpty()) {
                        text = "<" + Python3Parser.tokenNames[((TerminalNode)tree).getSymbol().getType()] + ">";
                    } else {
                        text = tree.getText().replace("\r", "").replace("\n", "\\n");
                    }

                    builder.append(indent)
                        .append(childStack.isEmpty() ? "'- " : "|- ")
                        .append(node.startsWith("terminal") ? text : node)
                        .append("\n");

                    if (tree.getChildCount() > 0) {
                        List<ParseTree> children = new ArrayList<>();
                        for (int i = 0; i < tree.getChildCount(); i++) {
                            children.add(tree.getChild(i));
                        }
                        childListStack.add(children);
                    }
                }
            }
        }
    }
}
