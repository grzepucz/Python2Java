package pl.agh.pythonparser;

import pl.agh.io.FileAccessor;
import pl.agh.pythonparser.Translator.Service.Translator;

import static pl.agh.pythonparser.Translator.Service.Translator.translate;

public class Main {

    public static void main(String[] args) {

        String source = "src/main/resources/python/Loops.py";
        String resultFile = "result";
        String sourceTree = "result_function_tree.txt";

        if (args.length > 0) {
            if (args[0] != null) {
                source = args[0];
            }

            if (args[1] != null) {
                resultFile = args[1];
            }

            if (args[2] != null) {
                sourceTree = args[1];
            }
        }

        translate(source, resultFile);

        FileAccessor fileAccessor = new FileAccessor();
        String content = fileAccessor.read(source);
        //fileAccessor.save(content.replace('d', 'c'), "result.txt");
        fileAccessor.save(new Builder.Tree(content).toStringASCII(), sourceTree);
    }
}
