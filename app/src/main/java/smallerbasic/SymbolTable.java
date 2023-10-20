package smallerbasic;

import org.jetbrains.annotations.NotNull;
import smallerbasic.AST.nodes.IdentifierASTNode;

import java.util.Iterator;
import java.util.Optional;

public interface SymbolTable<T> extends Iterable<T> {
    @NotNull String getBinding(@NotNull T id);

    @NotNull void newBinding(@NotNull T id);
}
