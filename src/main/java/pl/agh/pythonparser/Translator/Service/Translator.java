package pl.agh.pythonparser.Translator.Service;

import org.antlr.v4.runtime.tree.ParseTreeWalker;
import pl.agh.io.FileAccessor;
import pl.agh.pythonparser.Builder;
import pl.agh.pythonparser.Listener.Generator;
import pl.agh.pythonparser.Python3Parser;

/**
 *  Podstawowa klasa obslugujaca translacje
 */
public class Translator{

    /**
     * Skrócowana wersja, Metoda tlumaczy plik wskazany w sciezce i zapisuje go do domyslnego miejsca, pliku result
     * @param path
     * @return
     */
    public static String translate(String path) {
        return translate(path, "result");
    }

    /**
     * Metoda tlumaczy plik wskazany w sciezce i zapisuje go do pathTo
     * @param path
     * @param pathTo
     * @return
     */
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
