package pl.agh.pythonparser.Translator;

public interface Translatable {
    /**
     * Returns path to file with saved result.
     *
     * @param content String
     * @return String
     */
     String translate(String content);
}
