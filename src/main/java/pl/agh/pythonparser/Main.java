package pl.agh.pythonparser;

import pl.agh.io.FileAccessor;
import pl.agh.pythonparser.Translator.Service.Translator;

public class Main {


    public static void main(String[] args) {

        FileAccessor fileAccessor = new FileAccessor();

        String source = "src/main/resources/python/Function.py";

        Translator translator = new Translator();
        translator.translate(source);

        String content = fileAccessor.read(source);
        //fileAccessor.save(content.replace('d', 'c'), "result.txt");
        //fileAccessor.save(new Builder.Tree(content).toStringASCII(), "result_tree.txt");
    }
}
