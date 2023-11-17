package smallerbasic;

import org.antlr.v4.runtime.*;

/**
 * A Listener that listens for errors during parsing and prints them out with carets underneath the offending characters.
 */
public class PrettyErrorListener extends BaseErrorListener {

    private boolean hasFailed = false;

    /**
     * Query if any syntaxError has happened during the parsing.
     * @return {@code true} if {@link #syntaxError} was called at least once.
     */
    public boolean hasFailed() {
        return hasFailed;
    }

    // copied from "the definitive ANTLR 4 reference", pag 156
    @Override
    public void syntaxError(Recognizer<?, ?> recognizer,
                            Object offendingSymbol,
                            int line,
                            int charPositionInLine,
                            String msg,
                            RecognitionException e) {
        hasFailed = true;
        System.err.println("*** SyntaxError [" + line + ":"
                    + charPositionInLine + "]: " + msg);
        underlineError(recognizer, (Token) offendingSymbol, line, charPositionInLine);
    }

    private void underlineError(Recognizer<?, ?> recognizer,
                                Token offendingSymbol,
                                int line,
                                int charPositionInLine) {
        CommonTokenStream tokens = (CommonTokenStream) recognizer.getInputStream();
        String input = tokens.getTokenSource().getInputStream().toString();
        String[] lines = input.split("\n");
        String errorLine = lines[line - 1];
        System.err.println(errorLine);
        System.err.print(" ".repeat(charPositionInLine));
        int start = offendingSymbol.getStartIndex();
        int stop = offendingSymbol.getStopIndex();
        if (start >= 0 && stop >= 0)
            System.err.print("^".repeat((stop - start + 1)));
        System.err.println();
    }

}
