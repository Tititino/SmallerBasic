package smallerbasic.symbolTable;

import org.jetbrains.annotations.NotNull;

/**
 * A {@code ScopedName} is something that has a name, and should be associated with a scope.
 * For example in SmallerBasic labels are scoped.
 * @param node The node associated with the scope.
 * @param scope The scope.
 * @param <T> The type of the node.
 */
public record ScopedName<T extends HasSymbol>(@NotNull T node, @NotNull Scope scope) {}
