package smallerbasic.AST;

import org.jetbrains.annotations.NotNull;
import smallerbasic.AST.nodes.ASTNode;
import smallerbasic.AST.nodes.IdentifierASTNode;

import java.util.*;

public class VariableNames extends SymbolTableVisitor<IdentifierASTNode> {

    public VariableNames(@NotNull ASTNode node, @NotNull VarNameGenerator gen) {
        super(node, gen);
    }
    protected @NotNull List<IdentifierASTNode> getAll(@NotNull ASTNode n) {
        return new ArrayList<>(n.accept(new GetSymbols()));
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
