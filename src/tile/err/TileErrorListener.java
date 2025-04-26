package tile.err;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

import tile.app.Log;

import java.util.ArrayList;
import java.util.List;

public class TileErrorListener extends BaseErrorListener {
    private final List<String> errorMessages = new ArrayList<>();
    private boolean hasErrors = false;

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer,
                          Object offendingSymbol,
                          int line,
                          int col,
                          String msg,
                          RecognitionException e) {
        hasErrors = true;
        String errorMessage = Log.errorf(line + ":" + col + ": Syntax Error - " + msg);
        errorMessages.add(errorMessage);
    }

    public boolean hasErrors() {
        return hasErrors;
    }

    public List<String> getErrorMessages() {
        return new ArrayList<>(errorMessages);
    }

    public void clearErrors() {
        errorMessages.clear();
        hasErrors = false;
    }
}