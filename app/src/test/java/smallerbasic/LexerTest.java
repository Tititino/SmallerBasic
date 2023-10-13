package smallerbasic;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

public class LexerTest {
    @Test
    void numberLiteralTest() {
        SBGrammarLexer lexer = new SBGrammarLexer(CharStreams.fromString("+9e-18 3 1.3 -12 3.14E12"));

        assertThat(lexer.getAllTokens())
                .extracting(Token::getText)
                .containsExactly("+9e-18", "3", "1.3", "-12", "3.14E12");
    }

    @Test
    void stringLiteralTest() {
        SBGrammarLexer lexer = new SBGrammarLexer(CharStreams.fromString("\"\" \"ciao\" \"'%$&/(!=?'\""));

        assertThat(lexer.getAllTokens())
                .extracting(Token::getText)
                .containsExactly("\"\"", "\"ciao\"", "\"'%$&/(!=?'\"");
    }

    @Test
    void boolLiteralTest() {
        SBGrammarLexer lexer = new SBGrammarLexer(CharStreams.fromString("true false"));

        assertThat(lexer.getAllTokens())
                .extracting(Token::getText)
                .containsExactly("true", "false");
    }

    @ParameterizedTest
    @CsvSource({
            "1 + 3 * 4,(expression (expression (literal 1)) + (expression (expression (literal 3)) * (expression (literal 4))))",
            "1 / 3 - 4,(expression (expression (expression (literal 1)) / (expression (literal 3))) - (expression (literal 4)))",
    })
    void arithExprTest(String expr, String expected) {
        SBGrammarLexer lexer = new SBGrammarLexer(CharStreams.fromString(expr));
        SBGrammarParser parser = new SBGrammarParser(new CommonTokenStream(lexer));

        SBGrammarParser.ExpressionContext tree = parser.expression();
        assertThat(tree.toStringTree(parser)).isEqualTo(expected);
    }
}
