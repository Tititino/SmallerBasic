package smallerbasic;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

public class ParserTest {

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
                        "(assignmentStmt a = (arithExpression 10))"
                );
    }
}
