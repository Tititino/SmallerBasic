package smallerbasic;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Token;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class LexerTest {
    @Test
    void numberLiteral() {
        SBGrammarLexer lexer = new SBGrammarLexer(CharStreams.fromString("+9e-18 3 1.3 -12 3.14E12"));

        assertThat(lexer.getAllTokens())
                .extracting(Token::getText)
                .containsExactly("+9e-18", "3", "1.3", "-12", "3.14E12");
    }

    @Test
    void stringLiteral() {
        SBGrammarLexer lexer = new SBGrammarLexer(CharStreams.fromString("\"\" \"ciao\" \"'%$&/(!=?'\""));

        assertThat(lexer.getAllTokens())
                .extracting(Token::getText)
                .containsExactly("\"\"", "\"ciao\"", "\"'%$&/(!=?'\"");
    }

    @Test
    void boolLiteral() {
        SBGrammarLexer lexer = new SBGrammarLexer(CharStreams.fromString("true false"));

        assertThat(lexer.getAllTokens())
                .extracting(Token::getText)
                .containsExactly("true", "false");
    }
}
