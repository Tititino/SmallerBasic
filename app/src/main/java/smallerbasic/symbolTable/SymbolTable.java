package smallerbasic.symbolTable;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import smallerbasic.AST.ASTMonoidVisitor;
import smallerbasic.AST.nodes.*;

import java.util.*;

/**
 * The symbol table is a map associating entities in the AST to unique names.
 * Since the nodes are more or less immutable, the nodes themselves are put into the symbol table as keys.
 * Each label, function and variable is given a new name to avoid clashes.
 * Literals are also assigned names that refers to the global variable that contains them.
 */
public class SymbolTable {
    private final @NotNull VarNameGenerator gen;
    private final @NotNull Map<HasSymbol, String> bindings = new HashMap<>();

    /**
     * Get the name associated to a certain entity.
     * @param n The queried node.
     * @return The name associated to node {@code n} or {@code null} if {@code n} does not have a name.
     */
    public @Nullable String getBinding(@NotNull HasSymbol n) {
        return bindings.get(n);
    }

    /**
     * Assign a new name to a node.
     * @param id The node.
     */
    private void newBinding(@NotNull HasSymbol id) {
        bindings.put(id, gen.newName());
    }

    public SymbolTable(@NotNull ASTNode node, @NotNull VarNameGenerator gen) {
        this.gen = gen;
        List<HasSymbol> symbols = node.accept(new GetSymbols()).stream().toList();
        for (HasSymbol id : symbols)
            newBinding(id);
    }

    private static class GetSymbols implements ASTMonoidVisitor<Set<HasSymbol>> {
        @Override
        public Set<HasSymbol> empty() {
            return Collections.emptySet();
        }

        @Override
        public Set<HasSymbol> compose(Set<HasSymbol> o1, Set<HasSymbol> o2) {
            Set<HasSymbol> newSet = new HashSet<>(o1);
            newSet.addAll(o2);
            return newSet;
        }

        @Override
        public Set<HasSymbol> visit(RoutineNameASTNode n) {
            return Set.of(n);
        }
        @Override
        public Set<HasSymbol> visit(LabelNameASTNode n) {
            return Set.of(n);
        }
        @Override
        public Set<HasSymbol> visit(NumberLiteralASTNode n) {
            return Set.of(n);
        }
        @Override
        public Set<HasSymbol> visit(StringLiteralASTNode n) {
            return Set.of(n);
        }
        @Override
        public Set<HasSymbol> visit(BoolLiteralASTNode n) {
            return Set.of(n);
        }
        @Override
        public Set<HasSymbol> visit(IdentifierASTNode n) {
            return Set.of(n);
        }
    }
}
