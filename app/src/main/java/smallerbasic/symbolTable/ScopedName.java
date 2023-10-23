package smallerbasic.symbolTable;

import org.jetbrains.annotations.NotNull;

public record ScopedName(@NotNull HasSymbol node, @NotNull Scope scope) {}
