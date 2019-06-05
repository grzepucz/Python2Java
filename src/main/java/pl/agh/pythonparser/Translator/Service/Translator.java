package pl.agh.pythonparser.Translator.Service;

import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import pl.agh.io.FileAccessor;
import pl.agh.pythonparser.Builder;

import pl.agh.pythonparser.Listener.ParsingListener;
import pl.agh.pythonparser.Mapper.Mapper;
import pl.agh.pythonparser.Python3BaseListener;
import pl.agh.pythonparser.Python3Parser;
import pl.agh.pythonparser.Translator.Translatable;

import java.io.File;
import java.util.HashMap;

public class Translator implements Translatable {

    public Translator()
    {

    }

    @Override
    public String translate(String path) {
        Python3Parser parser = new Builder.Parser(
                new FileAccessor().read(path)
        ).build();

        ParseTreeWalker.DEFAULT.walk(
                new ParsingListener(),
                parser.file_input()
        );

        return "";
    }
}
