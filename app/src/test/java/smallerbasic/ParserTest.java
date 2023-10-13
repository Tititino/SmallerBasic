package smallerbasic;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

public class ParserTest {

    @ParameterizedTest
    @CsvSource({
            "1 + 3 * 4,(expression (expression 1) + (expression (expression 3) * (expression 4)))",
            "1 / 3 - 4,(expression (expression (expression 1) / (expression 3)) - (expression 4))",
    })
    void arithExprTest(String expr, String expected) {
        SBGrammarLexer lexer = new SBGrammarLexer(CharStreams.fromString(expr));
        SBGrammarParser parser = new SBGrammarParser(new CommonTokenStream(lexer));

        SBGrammarParser.ExpressionContext tree = parser.expression();
        assertThat(tree.toStringTree(parser)).isEqualTo(expected);
    }

    @Test
    void compExprTest() {
        SBGrammarLexer lexer = new SBGrammarLexer(CharStreams.fromString("1 + 2 <= 3 * 4"));
        SBGrammarParser parser = new SBGrammarParser(new CommonTokenStream(lexer));

        SBGrammarParser.BooleanExpressionContext tree = parser.booleanExpression();
        assertThat(tree.toStringTree(parser))
                .isEqualTo("(booleanExpression (expression (expression 1) + (expression 2)) <= (expression (expression 3) * (expression 4)))");
    }

    @Test
    void precedenceTest() {
        SBGrammarLexer lexer = new SBGrammarLexer(CharStreams.fromString("1 + 2 * 3 < 5 Or a = 1 + b + 2"));
        SBGrammarParser parser = new SBGrammarParser(new CommonTokenStream(lexer));
        SBGrammarParser.BooleanExpressionContext tree = parser.booleanExpression();

        assertThat(tree.toStringTree(parser))
                .isEqualTo(
                        // ((1 + (2 * 3)) < 5) Or (0 = ((1 + 1) + 2))
                        "(booleanExpression (booleanExpression (expression (expression 1) + (expression (expression 2) * (expression 3))) < (expression 5)) Or (booleanExpression (expression a) = (expression (expression (expression 1) + (expression b)) + (expression 2))))"
                );
    }
}
