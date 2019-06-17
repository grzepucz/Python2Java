package pl.agh.pythonparser;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

/**
 * Listener, przy napotkaniu bledu porzuca parsowanie i zwraca kod. Nie zachowuje kontekstów
 */
public class DescriptiveBailErrorListener extends BaseErrorListener {

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                            int line, int charPositionInLine,
                            String msg, RecognitionException e) {

        String entireMessage = String.format("source: %s, line: %s, index: %s, error message: %s %s",
                recognizer.getInputStream().getSourceName(), line, charPositionInLine, msg,
                e == null ? "<null>" : e.getMessage());

        throw new RuntimeException(entireMessage);
    }
}