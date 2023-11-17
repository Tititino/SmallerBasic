package smallerbasic;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static smallerbasic.CompilationUtils.lex;

public class ParserTest {

    /**
     * Basically halt and catch fire when it encounters any error.
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
    @Disabled
    void arithExprTest(String expr, String expected) {
        SBGrammarLexer lexer = new SBGrammarLexer(CharStreams.fromString(expr));
        SBGrammarParser parser = new SBGrammarParser(new CommonTokenStream(lexer));
        ParserRuleContext tree = parser.expression();

        assertThat(tree.toStringTree(parser)).isEqualTo(expected);
    }

    @Test
    void compExprTest() {
        SBGrammarLexer lexer = new SBGrammarLexer(CharStreams.fromString("1 + 2 <= 3 * 4"));
        SBGrammarParser parser = new SBGrammarParser(new CommonTokenStream(lexer));
        ParserRuleContext tree = parser.expression();

        assertThat(tree.toStringTree(parser))
                .isEqualTo("(expression " +
                        "(expression (expression (atom 1)) + (expression (atom 2))) " +
                        "<= (expression (expression (atom 3)) * (expression (atom 4))))");
    }

    @Test
    void precedenceTest() {
        SBGrammarLexer lexer = new SBGrammarLexer(CharStreams.fromString("1 + 2 * 3 < 5 Or a = 1 + b + 2"));
        SBGrammarParser parser = new SBGrammarParser(new CommonTokenStream(lexer));
        ParserRuleContext tree = parser.expression();

        assertThat(tree.toStringTree(parser))
                .isEqualTo(
                        "(expression (expression (expression (expression (atom 1)) " +
                                "+ (expression (expression (atom 2)) * (expression (atom 3)))) " +
                                "< (expression (atom 5))) Or (expression (expression (atom (variable (varName a)))) " +
                                "= (expression (expression (expression (atom 1)) + (expression (atom (variable (varName b))))) " +
                                "+ (expression (atom 2)))))"
                );
    }

    @Test
    void assignmentTest() {
        SBGrammarLexer lexer = new SBGrammarLexer(CharStreams.fromString("a = 10"));
        SBGrammarParser parser = new SBGrammarParser(new CommonTokenStream(lexer));
        ParserRuleContext tree = parser.assignmentStmt();

        assertThat(tree.toStringTree(parser))
                .isEqualTo(
                        "(assignmentStmt (variable (varName a)) = (expression (atom 10)))"
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

    @ParameterizedTest
    @ValueSource(strings = {
            "arraySetTest.sb",
            "bigTest.sb",
            "forLoopTest.sb",
            "illegalLabelsTest.sb",
            "multiArrayTest.sb",
            "nestedForTest.sb",
            "subRoutineTest.sb",
            "test1.sb",
            "test2.sb",
            "test3.sb",
            "test4.sb",
            "uninitializedVarTest.sb",
            "whileTest.sb"
    })
    void abiguityTest(String path) throws IOException {
        TokenStream tokens = lex(Paths.get("src/test/resources/" + path));
        SBGrammarParser parser = new SBGrammarParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(new DiagnosticErrorListener());
        parser.getInterpreter().setPredictionMode(PredictionMode.LL_EXACT_AMBIG_DETECTION);
        parser.program();
    }
}
