package smallerbasic.compiler;

import org.jetbrains.annotations.NotNull;
import smallerbasic.AST.nodes.ASTNode;

/**
 * Converts an {@link ASTNode} to a string in a way or another.
 */
public interface ASTToString {
    String run(@NotNull ASTNode n);
}
