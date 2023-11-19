package smallerbasic.compiler.LLVM;

import org.jetbrains.annotations.NotNull;
import smallerbasic.AST.nodes.ASTNode;

/**
 * Converts an {@link ASTNode} to a string in a way or another.
 */
interface ASTToString {
    String run(@NotNull ASTNode n);
}
