package smallerbasic.compiler;

import org.jetbrains.annotations.NotNull;
import smallerbasic.AST.nodes.ASTNode;

/**
 * Compiles an {@link ASTNode} to something.
 * Given a correct tree a {@link Compiler} should return a string representing the program representing that tree.
 */
public interface Compiler {
     String compile(@NotNull ASTNode root);
}
