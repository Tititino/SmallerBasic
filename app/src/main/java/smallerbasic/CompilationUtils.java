package smallerbasic;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.jetbrains.annotations.NotNull;
import smallerbasic.AST.ParseTreeToASTVisitorWithTokens;
import smallerbasic.AST.nodes.ASTNode;
import smallerbasic.AST.staticChecks.Check;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * This class groups together some utility methods to lex, compile and check a SmallerBasic program..
 */
public class CompilationUtils {
    public static @NotNull TokenStream lex(@NotNull Path path) throws IOException {
        return new CommonTokenStream(new SBGrammarLexer(CharStreams.fromFileName(path.toString())));
    }

    public static @NotNull TokenStream lex(@NotNull String text) {
        return new CommonTokenStream(new SBGrammarLexer(CharStreams.fromString(text)));
    }

    public static @NotNull Optional<ParseTree> parse(@NotNull TokenStream tokens) {
        SBGrammarParser parser = new SBGrammarParser(tokens);
        PrettyErrorListener listener = new PrettyErrorListener();
        parser.removeErrorListeners();
        parser.addErrorListener(listener);
        ParseTree tree = parser.program();
        System.out.println("result: " + listener.hasFailed());
        if (listener.hasFailed())
            return Optional.empty();
        return Optional.of(tree);
    }

    public static @NotNull ASTNode clean(@NotNull ParseTree tree) {
        return (new ParseTreeToASTVisitorWithTokens()).visit(tree);
    }

    public static @NotNull Optional<ASTNode> check(@NotNull ASTNode tree,
                                                   @NotNull List<Check> errors,
                                                   @NotNull List<Check> warnings) {
        // directly using allMatch stops the checks at the first failed one
        List<Boolean> allPass = errors
                .stream()
                .map(x -> x.check(tree))
                .toList();
        // warning do not end compilation
        warnings.forEach(x -> x.check(tree));
        return allPass.stream().allMatch(x -> x) ? Optional.of(tree) : Optional.empty();
    }
}
