package pl.agh.pythonparser.Translator.Service;

import org.antlr.v4.runtime.tree.ParseTreeWalker;
import pl.agh.io.FileAccessor;
import pl.agh.pythonparser.Builder;
import pl.agh.pythonparser.Listener.Generator;
import pl.agh.pythonparser.Python3Parser;

public class Translator{

    public static String translate(String path) {
        return translate(path, "result");
    }

    public static String translate(String path, String pathTo) {
        Python3Parser parser = new Builder.Parser(
                new FileAccessor().read(path)
        ).build();

        ParseTreeWalker.DEFAULT.walk(
                new Generator(pathTo),
                parser.file_input()
        );

        return pathTo;
    }
}
