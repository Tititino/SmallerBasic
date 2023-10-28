package smallerbasic;

import org.antlr.v4.runtime.*;
import java.util.Objects;

public class PrettyErrorListener extends BaseErrorListener {
    // copied from "the definitive ANTLR 4 reference", pag 156
    @Override
    public void syntaxError(Recognizer<?, ?> recognizer,
                            Object offendingSymbol,
                            int line,
                            int charPositionInLine,
                            String msg,
                            RecognitionException e) {
        if (!Objects.isNull(e)) {
            String ctxString = printCtx((ParserRuleContext) e.getCtx());
            if (!Objects.isNull(ctxString)) {
                System.err.println("*** SyntaxError [" + line + ":"
                        + charPositionInLine + "] error while parsing " + ctxString + ": " + msg);
                underlineError(recognizer, (Token) offendingSymbol, line, charPositionInLine);
                return;
            }
        }
        System.err.println("*** SyntaxError [" + line + ":"
                    + charPositionInLine + "]: " + msg);
        underlineError(recognizer, (Token) offendingSymbol, line, charPositionInLine);
    }

    private String printCtx(ParserRuleContext ctx) {
        if (ctx instanceof SBGrammarParser.AssignmentStmtContext)
            return "an assignment";
        if (ctx instanceof SBGrammarParser.ProgramContext)
            return "the program";
        if (ctx instanceof SBGrammarParser.IfStmtContext)
            return "an if statement";
        if (ctx instanceof SBGrammarParser.WhileStmtContext)
            return "a while loop";
        if (ctx instanceof SBGrammarParser.ForStmtContext)
            return "a for loop";
        if (ctx instanceof SBGrammarParser.GotoStmtContext)
            return "a goto";
        if (ctx instanceof SBGrammarParser.LabelContext)
            return "a label declaration";
        if (ctx instanceof SBGrammarParser.SubroutineDeclContext)
            return "a subroutine declaration";
        if (ctx instanceof SBGrammarParser.ArrayContext)
            return "an array";
        return null;
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
