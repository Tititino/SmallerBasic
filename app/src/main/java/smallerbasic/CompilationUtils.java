package smallerbasic;

import org.antlr.v4.runtime.*;
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
     *
     * @param path The path of the file.
     * @return The {@link TokenStream} generated.
     * @throws IOException propagates any exception risen by the file handling.
     */
    public static @NotNull TokenStream lex(@NotNull Path path) throws IOException {
        return lex(CharStreams.fromFileName(path.toString()));
    }

    private static @NotNull TokenStream lex(@NotNull CharStream chars) {
        SBGrammarLexer lexer = new SBGrammarLexer(chars);
        lexer.removeErrorListeners();
        BaseErrorListener listener = new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer,
                                    Object offendingSymbol,
                                    int line,
                                    int charPositionInLine,
                                    String msg,
                                    RecognitionException e) {
                System.err.println("*** LexError [" + line + ":"
                        + charPositionInLine + "]: " + msg);
            }
        };
        lexer.addErrorListener(listener);
        return new CommonTokenStream(lexer);
    }

    /**
     * Lex a source given as a string.
     *
     * @param text The source.
     * @return The corresponding {@link TokenStream}.
     */
    public static @NotNull TokenStream lex(@NotNull String text) {
        return lex(CharStreams.fromString(text));
    }

    /**
     * Parse a {@link TokenStream} and return the {@link ParseTree}.
     *
     * @param tokens The token stream.
     * @return {@code Optional.empty()} if there have been any parsing errors, the {@link ParseTree} otherwise.
     */
    public static @NotNull ParseTree parse(@NotNull TokenStream tokens) {
        SBGrammarParser parser = new SBGrammarParser(tokens);
        PrettyErrorListener listener = new PrettyErrorListener();
        parser.removeErrorListeners();
        parser.addErrorListener(listener);
        ParseTree tree = parser.program();
        if (listener.hasFailed())
            throw new CompilationError("Parsing failed");
        return tree;
    }

    /**
     * Transforms the {@link ParseTree} to an AST.
     *
     * @param tree The {@link ParseTree}, it must be well-formed. The behaviour otherwise is undefined.
     * @return The {@link ASTNode} root.
     */
    public static @NotNull ASTNode clean(@NotNull ParseTree tree) {
        return (new ParseTreeToASTVisitorWithTokens()).visit(tree);
    }

    /**
     * Given an {@link ASTNode}, a list of errors and a list of warnings, applies each check to the tree.
     *
     * @param tree     The tree to be checked.
     * @param errors   A list of checks that the tree MUST pass.
     * @param warnings A list of checks that the tree may not pass.
     * @return {@link Optional#empty()} if at least one check in {@code errors} fails.
     */
    public static @NotNull ASTNode check(@NotNull ASTNode tree,
                                         @NotNull List<Check> errors,
                                         @NotNull List<Check> warnings) {
        // directly using allMatch stops the checks at the first failed one
        List<Boolean> allPass = errors
                .stream()
                .map(x -> x.check(tree))
                .toList();
        // warnings do not halt compilation
        warnings.forEach(x -> x.check(tree));
        if (!allPass.stream().allMatch(x -> x))
            throw new CompilationError("Static checks failed");
        return tree;
    }

    /**
     * Given a {@link Compiler} compiles the {@link ASTNode}.
     *
     * @param tree The AST to compile.
     * @param c    A compiler.
     * @return A string corresponding to the program represented by {@code tree}.
     */
    public static @NotNull String compile(@NotNull ASTNode tree, @NotNull Compiler c) {
        return c.compile(tree);
    }
}
