package smallerbasic.AST;

import org.jetbrains.annotations.NotNull;
import smallerbasic.AST.nodes.ASTNode;
import smallerbasic.AST.nodes.IdentifierASTNode;

import java.util.*;

public class SymbolTable implements Iterable<IdentifierASTNode> {

    private final @NotNull List<IdentifierASTNode> symbols;

    public SymbolTable(@NotNull ASTNode node) {
        this.symbols = node.accept(new GetSymbols()).stream().toList();
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
