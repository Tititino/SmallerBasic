package smallerbasic.compiler.LLVM;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import smallerbasic.AST.ASTMonoidVisitor;
import smallerbasic.AST.nodes.*;

import java.util.*;

/**
 * The symbol table is a map associating entities in the AST to unique names.
 * Since the nodes are more or less immutable, the nodes themselves are put into the symbol table as keys.
 * Each label, function name and variable is given a new name to avoid clashes.
 * Literals are also assigned names that refer to the global variable that contains them.
 */
class SymbolTable {
    private final @NotNull VarNameGenerator gen;
    private final @NotNull Map<ASTNode, String> bindings = new HashMap<>();

    /**
     * Get the name associated to a certain entity.
     * @param n The queried node.
     * @return The name associated to node {@code n} or {@code null} if {@code n} does not have a name.
     */
    public @Nullable String getBinding(@NotNull ASTNode n) {
        return bindings.get(n);
    }

    /**
     * Assign a new name to a node.
     * @param id The node.
     */
    private void newBinding(@NotNull ASTNode id) {
        bindings.put(id, gen.newName());
    }

    public SymbolTable(@NotNull ASTNode node, @NotNull VarNameGenerator gen) {
        this.gen = gen;
        List<ASTNode> symbols = node.accept(new GetSymbols()).stream().toList();
        for (ASTNode id : symbols)
            newBinding(id);
    }

    private static class GetSymbols implements ASTMonoidVisitor<Set<ASTNode>> {
        @Override
        public Set<ASTNode> empty() {
            return Collections.emptySet();
        }

        @Override
        public Set<ASTNode> compose(Set<ASTNode> o1, Set<ASTNode> o2) {
            Set<ASTNode> newSet = new HashSet<>(o1);
            newSet.addAll(o2);
            return newSet;
        }

        @Override
        public Set<ASTNode> visit(RoutineNameASTNode n) {
            return Set.of(n);
        }
        @Override
        public Set<ASTNode> visit(LabelNameASTNode n) {
            return Set.of(n);
        }
        @Override
        public Set<ASTNode> visit(NumberLiteralASTNode n) {
            return Set.of(n);
        }
        @Override
        public Set<ASTNode> visit(StringLiteralASTNode n) {
            return Set.of(n);
        }
        @Override
        public Set<ASTNode> visit(BoolLiteralASTNode n) {
            return Set.of(n);
        }
        @Override
        public Set<ASTNode> visit(IdentifierASTNode n) {
            return Set.of(n);
        }
    }
}
