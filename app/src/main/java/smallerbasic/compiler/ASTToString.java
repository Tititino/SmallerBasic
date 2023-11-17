package smallerbasic.compiler;

import org.jetbrains.annotations.NotNull;
import smallerbasic.AST.nodes.ASTNode;

/**
 * Converts an {@link ASTNode} to some sort of string.
 */
public interface ASTToString {
    String run(@NotNull ASTNode n);
}
