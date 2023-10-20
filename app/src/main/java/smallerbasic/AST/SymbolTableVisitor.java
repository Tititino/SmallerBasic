package smallerbasic.AST;

import org.jetbrains.annotations.NotNull;
import smallerbasic.AST.nodes.ASTNode;
import smallerbasic.AST.nodes.IdentifierASTNode;
import smallerbasic.PrintableToLLVM;
import smallerbasic.SymbolTable;

import java.util.*;

public class SymbolTableVisitor implements SymbolTable<IdentifierASTNode> {

    private final @NotNull VarNameGenerator gen = new VarNameGenerator();
    private final @NotNull List<IdentifierASTNode> symbols;
    private final @NotNull Map<IdentifierASTNode, String> bindings = new HashMap<>();

    public @NotNull String getBinding(@NotNull IdentifierASTNode id) {
        return bindings.get(id);
    }

    public @NotNull void newBinding(@NotNull IdentifierASTNode id) {
        bindings.put(id, gen.newName());
    }

    public SymbolTableVisitor(@NotNull ASTNode node) {
        this.symbols = node.accept(new GetSymbols()).stream().toList();
        for (IdentifierASTNode id : symbols)
            newBinding(id);
    }

    @NotNull
    @Override
    public Iterator<IdentifierASTNode> iterator() {
        return symbols.iterator();  // intellij says using Collections.unmodifiableList is redundant
    }

    private static class GetSymbols implements ASTMonoidVisitor<Set<IdentifierASTNode>> {
        @Override
        public Set<IdentifierASTNode> empty() {
            return Collections.emptySet();
        }

        @Override
        public Set<IdentifierASTNode> compose(Set<IdentifierASTNode> o1, Set<IdentifierASTNode> o2) {
            Set<IdentifierASTNode> newSet = new HashSet<>(o1);
            newSet.addAll(o2);
            return newSet;
        }

        @Override
        public Set<IdentifierASTNode> visit(IdentifierASTNode n) {
            return Set.of(n);
        }
    }
}
