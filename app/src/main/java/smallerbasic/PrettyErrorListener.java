package smallerbasic;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;

import java.util.BitSet;

public class PrettyErrorListener extends BaseErrorListener {

    private boolean hasFailed = false;

    public boolean hasFailed() {
        return hasFailed;
    }

    @Override
    public void reportAmbiguity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, boolean exact, BitSet ambigAlts, ATNConfigSet configs) {
        hasFailed = true;
        super.reportAmbiguity(recognizer, dfa, startIndex, stopIndex, exact, ambigAlts, configs);
    }

    @Override
    public void reportAttemptingFullContext(Parser recognizer, DFA dfa, int startIndex, int stopIndex, BitSet conflictingAlts, ATNConfigSet configs) {
        hasFailed = true;
        super.reportAttemptingFullContext(recognizer, dfa, startIndex, stopIndex, conflictingAlts, configs);
    }

    @Override
    public void reportContextSensitivity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, int prediction, ATNConfigSet configs) {
        hasFailed = true;
        super.reportContextSensitivity(recognizer, dfa, startIndex, stopIndex, prediction, configs);
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
