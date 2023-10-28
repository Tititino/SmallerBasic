package smallerbasic;

import org.antlr.v4.runtime.*;

/**
 * This class prints out the errors with carets underneath the offending characters.
 * There is also a method, {@see hasFailed}, that {@code true} if there have been any errors.
 * This is used to prevent passing a malformed parse tree to {@link smallerbasic.AST.ParseTreeToASTVisitor}.
 */
public class PrettyErrorListener extends BaseErrorListener {

    private boolean hasFailed = false;

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
        for (int i = 0; i < charPositionInLine; i++) System.err.print(" ");
        int start = offendingSymbol.getStartIndex();
        int stop = offendingSymbol.getStopIndex();
        if (start >= 0 && stop >= 0)
            for (int i = start; i <= stop; i++) System.err.print("^");
        System.err.println();
    }

}
