package smallerbasic;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Token;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class LexerTest {
    @Test
    void numberLiteralTest() {
        SBGrammarLexer lexer = new SBGrammarLexer(CharStreams.fromString("+9e-18 3 1.3 12 3.14E12"));

        assertThat(lexer.getAllTokens())
                .extracting(Token::getText)
                .containsExactly("+9e-18", "3", "1.3", "12", "3.14E12");
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

    @Test
    void variableNameTest() {
        SBGrammarLexer lexer = new SBGrammarLexer(CharStreams.fromString("a b _c C1A0"));

        assertThat(lexer.getAllTokens())
                .extracting(Token::getText)
                .containsExactly("a", "b", "_c", "C1A0");
    }

}
