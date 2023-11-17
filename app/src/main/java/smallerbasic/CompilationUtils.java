package smallerbasic;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.jetbrains.annotations.NotNull;
import smallerbasic.AST.ParseTreeToASTVisitorWithTokens;
import smallerbasic.AST.nodes.ASTNode;
import smallerbasic.AST.staticChecks.Check;
import smallerbasic.compiler.Compiler;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * This class groups together some utility methods to lex, compile and check a SmallerBasic program.
 */
public class CompilationUtils {
    /**
     * Lex a source given as path to the file.
     * @param path The path of the file.
     * @return The {@link TokenStream} generated.
     * @throws IOException propagates any exception risen by the file handling.
     */
    public static @NotNull TokenStream lex(@NotNull Path path) throws IOException {
        return new CommonTokenStream(new SBGrammarLexer(CharStreams.fromFileName(path.toString())));
    }

    /**
     * Lex a source given as a string.
     * @param text The source.
     * @return The corresponding {@link TokenStream}.
     */
    public static @NotNull TokenStream lex(@NotNull String text) {
        return new CommonTokenStream(new SBGrammarLexer(CharStreams.fromString(text)));
    }

    /**
     * Parse a {@link TokenStream} and return the {@link ParseTree}.
     * @param tokens The token stream.
     * @return {@code Optional.empty()} if there have been any parsing errors, the {@link ParseTree} otherwise.
     */
    public static @NotNull Optional<ParseTree> parse(@NotNull TokenStream tokens) {
        SBGrammarParser parser = new SBGrammarParser(tokens);
        PrettyErrorListener listener = new PrettyErrorListener();
        parser.removeErrorListeners();
        parser.addErrorListener(listener);
        ParseTree tree = parser.program();
        if (listener.hasFailed())
            return Optional.empty();
        return Optional.of(tree);
    }

    /**
     * Transforms the {@link ParseTree} to an AST.
     * @param tree The {@link ParseTree}, it must be well-formed. The behaviour otherwise is undefined.
     * @return The {@link ASTNode} root.
     */
    public static @NotNull ASTNode clean(@NotNull ParseTree tree) {
        return (new ParseTreeToASTVisitorWithTokens()).visit(tree);
    }

    /**
     * Given an {@link ASTNode}, a list of errors and a list of warnings, applies each check to the tree.
     * @param tree The tree to be checked.
     * @param errors A list of checks that the tree MUST pass.
     * @param warnings A list of checks that the tree may not pass.
     * @return {@link Optional#empty()} if at least one error check fails.
     */
    public static @NotNull Optional<ASTNode> check(@NotNull ASTNode tree,
                                                   @NotNull List<Check> errors,
                                                   @NotNull List<Check> warnings) {
        // directly using allMatch stops the checks at the first failed one
        List<Boolean> allPass = errors
                .stream()
                .map(x -> x.check(tree))
                .toList();
        // warnings do not halt compilation
        warnings.forEach(x -> x.check(tree));
        return allPass.stream().allMatch(x -> x) ? Optional.of(tree) : Optional.empty();
    }

    /**
     * Given a {@link Compiler} compiler the {@link ASTNode}.
     * @param tree The AST to compile.
     * @param c A compiler.
     * @return A string corresponding to the program represented by {@code tree}.
     */
    public static @NotNull String compile(@NotNull ASTNode tree, @NotNull Compiler c) {
        return c.compile(tree);
    }
}
