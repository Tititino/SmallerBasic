package smallerbasic;

import org.jetbrains.annotations.NotNull;
import smallerbasic.AST.nodes.ASTNode;

public record ScopedName(@NotNull ASTNode node, @NotNull Scope scope) {}
