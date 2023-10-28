package smallerbasic.AST.staticChecks;

import org.antlr.v4.runtime.Token;
import org.jetbrains.annotations.NotNull;
import smallerbasic.AST.nodes.ASTNode;

public interface ErrorReporter {
    ErrorReporter STDERR_REPORTER = (n, msg) -> {
        String pos = "";
        if (n.getStartToken().isPresent()) {
            Token start = n.getStartToken().get();
            pos = " [" + start.getLine() + ":" + start.getCharPositionInLine() + "]";
        }
        System.err.println(msg + pos);
    };

    void reportError(@NotNull ASTNode n, @NotNull String msg);
}
