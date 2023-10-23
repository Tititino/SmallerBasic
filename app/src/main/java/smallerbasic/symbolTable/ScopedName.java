package smallerbasic.symbolTable;

import org.jetbrains.annotations.NotNull;

public record ScopedName<T>(@NotNull T node, @NotNull Scope scope) {}
