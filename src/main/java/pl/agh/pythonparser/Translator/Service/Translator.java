package pl.agh.pythonparser.Translator.Service;

import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import pl.agh.io.FileAccessor;
import pl.agh.pythonparser.Builder;
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

        ParseTreeWalker.DEFAULT.walk(new Python3BaseListener() {

            @Override
            public void enterFuncdef(@NotNull Python3Parser.FuncdefContext ctx) {
                System.out.println("FUNCNAME=" + ctx.getText());
            }
            @Override
            public void enterSingle_input(@NotNull Python3Parser.Single_inputContext ctx) {
                System.out.println("Single input=" + ctx.getText());
            }
            @Override
            public void enterParameters(@NotNull Python3Parser.ParametersContext ctx) {
                System.out.println("PARAMS=" + ctx.typedargslist().getText());
            }
            @Override
            public void enterSimple_stmt(@NotNull Python3Parser.Simple_stmtContext ctx) {
                System.out.println("statement=" + ctx.getText());
                System.out.println("statementRI=" + ctx.getRuleIndex());
                System.out.println("statementRC=" + ctx.getRuleContext().getText());
            }
        }, parser.file_input());

        return "";
    }
}
