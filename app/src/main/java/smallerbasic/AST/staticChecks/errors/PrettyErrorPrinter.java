package smallerbasic.AST.staticChecks.errors;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.jetbrains.annotations.NotNull;
import smallerbasic.AST.nodes.ASTNode;
import smallerbasic.PrettyErrorListener;

/**
 * {@link ErrorReporter} that prints error in a similar fashion to {@link PrettyErrorListener}.
 */
public class PrettyErrorPrinter implements ErrorReporter {
    private final @NotNull String[] lines;

    public PrettyErrorPrinter(@NotNull TokenStream tokens) {
        lines = tokens.getTokenSource().getInputStream().toString().split("\n");
    }

    /**
     * Prints an error position spanning multiple lines in the following fashion:
     * <pre>
     * {@code
     *    from line <start> to line <end>
     * <start line>
     * ...
     * <end line>
     * }
     * </pre>
     * @param start the start token.
     * @param end the end token.
     */
    private void printDifferentLines(@NotNull Token start, @NotNull Token end) {
        System.err.println("    from line " + start.getLine() + " to line " + end.getLine());
        String startLine = lines[start.getLine() - 1];
        String endLine   = lines[end.getLine() - 1];
        System.err.println(startLine);
        System.err.println("...");
        System.err.println(endLine);
        System.err.println();
    }

    /**
     * Prints an error position located on a single line in the following fashion:
     * <pre>
     * {@code
     *    at line <line>:<start>-<end>
     * <line>
     * <underlining>
     * }
     * </pre>
     * @param start the start token.
     * @param end the end token.
     */
    private void printSameLines(@NotNull Token start, @NotNull Token end) {
        String position = start.getLine() + ":" + start.getCharPositionInLine() + "-" + (end.getCharPositionInLine() + end.getText().length() - 1);
        System.err.println("    at line " + position);
        String errorLine = lines[start.getLine() - 1];
        System.err.println(errorLine);
        System.err.print(" ".repeat(start.getCharPositionInLine()));
        System.err.print("^".repeat(end.getCharPositionInLine() + end.getText().length() - start.getCharPositionInLine()));
        System.err.println();
    }

    /**
     * Report an error highlighting where in the source code it is located.
     * @param n The faulty node.
     * @param msg A description of the error.
     */
    @Override
    public void reportError(@NotNull ASTNode n, @NotNull String msg) {
        System.err.println(msg);
        if (n.getStartToken().isPresent() && n.getEndToken().isPresent()) {
            Token start = n.getStartToken().get();
            Token end = n.getEndToken().get();
            if (start.getLine() == end.getLine())
                printSameLines(start, end);
            else
                printDifferentLines(start, end);
        }
    }
}
