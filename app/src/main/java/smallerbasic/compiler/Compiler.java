package smallerbasic.compiler;

import org.jetbrains.annotations.NotNull;
import smallerbasic.AST.nodes.ASTNode;

/**
 * Compiles an {@link ASTNode} to something.
 */
public interface Compiler {
     String compile(@NotNull ASTNode root);
}
