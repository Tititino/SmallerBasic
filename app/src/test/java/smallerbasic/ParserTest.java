package smallerbasic;

import org.antlr.v4.runtime.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

public class ParserTest {

    /**
     * Basically halt anc catch fire when it encounters any error.
     * Could be done in a prettier way with an ANTLRErrorListener.
     */
    static class ExceptionErrorHandler extends DefaultErrorStrategy {
        @Override
        public void reportError(Parser recognizer, RecognitionException e) {
            throw e;
        }
        @Override
        protected void reportUnwantedToken(Parser recognizer) {
            throw new RuntimeException();
        }

        @Override
        protected void reportMissingToken(Parser recognizer) {
            throw new RuntimeException();
        }
    }

    static final ExceptionErrorHandler errorHandler = new ExceptionErrorHandler();

    @ParameterizedTest
    @CsvSource({
            "1 + 3 * 4,(arithExpression (arithExpression 1) + (arithExpression (arithExpression 3) * (arithExpression 4)))",
            "1 / 3 - 4,(arithExpression (arithExpression (arithExpression 1) / (arithExpression 3)) - (arithExpression 4))",
    })
    void arithExprTest(String expr, String expected) {
        SBGrammarLexer lexer = new SBGrammarLexer(CharStreams.fromString(expr));
        SBGrammarParser parser = new SBGrammarParser(new CommonTokenStream(lexer));
        ParserRuleContext tree = parser.arithExpression();

        assertThat(tree.toStringTree(parser)).isEqualTo(expected);
    }

    @Test
    void compExprTest() {
        SBGrammarLexer lexer = new SBGrammarLexer(CharStreams.fromString("1 + 2 <= 3 * 4"));
        SBGrammarParser parser = new SBGrammarParser(new CommonTokenStream(lexer));
        ParserRuleContext tree = parser.booleanExpression();

        assertThat(tree.toStringTree(parser))
                .isEqualTo("(booleanExpression (arithExpression (arithExpression 1) + (arithExpression 2)) <= (arithExpression (arithExpression 3) * (arithExpression 4)))");
    }

    @Test
    void precedenceTest() {
        SBGrammarLexer lexer = new SBGrammarLexer(CharStreams.fromString("1 + 2 * 3 < 5 Or a = 1 + b + 2"));
        SBGrammarParser parser = new SBGrammarParser(new CommonTokenStream(lexer));
        ParserRuleContext tree = parser.booleanExpression();

        assertThat(tree.toStringTree(parser))
                .isEqualTo(
                        // ((1 + (2 * 3)) < 5) Or (0 = ((1 + 1) + 2))
                        "(booleanExpression (booleanExpression (arithExpression (arithExpression 1) + (arithExpression (arithExpression 2) * (arithExpression 3))) < (arithExpression 5)) Or (booleanExpression (arithExpression a) = (arithExpression (arithExpression (arithExpression 1) + (arithExpression b)) + (arithExpression 2))))"
                );
    }

    @Test
    void assignmentTest() {
        SBGrammarLexer lexer = new SBGrammarLexer(CharStreams.fromString("a = 10"));
        SBGrammarParser parser = new SBGrammarParser(new CommonTokenStream(lexer));
        ParserRuleContext tree = parser.assignmentStmt();

        assertThat(tree.toStringTree(parser))
                .isEqualTo(
                        "(assignmentStmt a = (expression (arithExpression 10)))"
                );
    }

    @Test
    void statementsTest() {
        SBGrammarLexer lexer = new SBGrammarLexer(CharStreams.fromString(
                """
                       For A = 1 To 10
                          For B = 1 To 10
                            C = 3 * 4
                            Goto fine
                          EndFor
                       EndFor
                       B = 1 + 2
                       fine:
                       D = 4
                       """
        ));
        SBGrammarParser parser = new SBGrammarParser(new CommonTokenStream(lexer));
        parser.setErrorHandler(errorHandler);

        assertThatNoException().isThrownBy(parser::program);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "If (true) Then\n Else\n EndIf\n",
            "If (false) Then\n EndIf\n",
            "While (true)\n EndWhile\n",
            "For I = 2 To 10\n EndFor\n",
            "For I = 2 To 20 Step 10\n EndFor\n"
    })
    void emptyStatementTest(String test) {
        SBGrammarLexer lexer = new SBGrammarLexer(CharStreams.fromString(test));
        SBGrammarParser parser = new SBGrammarParser(new CommonTokenStream(lexer));
        parser.setErrorHandler(errorHandler);

        assertThatNoException().isThrownBy(parser::program);
    }

}
