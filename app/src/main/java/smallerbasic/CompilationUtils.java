package smallerbasic;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.TokenStream;
import org.jetbrains.annotations.NotNull;
import smallerbasic.AST.nodes.ASTNode;

import java.io.IOException;

public class CompilationUtils {
    public static @NotNull TokenStream lex(@NotNull String path) throws IOException {
        return new CommonTokenStream(new SBGrammarLexer(CharStreams.fromFileName(path)));
    }

    public static @NotNull ParserRuleContext parse(@NotNull TokenStream tokens) {
        return (new SBGrammarParser(tokens)).program();
    }

    public static @NotNull ASTNode clean(@NotNull ParserRuleContext tree) {
        return (new ParseTreeToASTVisitor()).visitProgram((SBGrammarParser.ProgramContext) tree);
    }
}
