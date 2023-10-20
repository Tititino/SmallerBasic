package smallerbasic.AST;

import org.jetbrains.annotations.NotNull;
import smallerbasic.AST.nodes.ASTNode;
import smallerbasic.SymbolTable;

import java.util.*;

public abstract class SymbolTableVisitor<T> implements SymbolTable<T> {

    private final @NotNull VarNameGenerator gen;
    private final @NotNull List<T> symbols;
    private final @NotNull Map<T, String> bindings = new HashMap<>();

    public @NotNull String getBinding(@NotNull T id) {
        return bindings.get(id);
    }

    public void newBinding(@NotNull T id) {
        bindings.put(id, gen.newName());
    }

    public SymbolTableVisitor(@NotNull ASTNode node, @NotNull VarNameGenerator gen) {
        this.gen = gen;
        this.symbols = getAll(node);
        for (T id : symbols)
            newBinding(id);
    }

    protected abstract @NotNull List<T> getAll(@NotNull ASTNode n);

    @NotNull
    @Override
    public Iterator<T> iterator() {
        return symbols.iterator();  // intellij says using Collections.unmodifiableList is redundant
    }


}
