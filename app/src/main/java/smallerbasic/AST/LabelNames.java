package smallerbasic.AST;

import org.jetbrains.annotations.NotNull;
import smallerbasic.AST.nodes.ASTNode;
import smallerbasic.AST.nodes.LabelDeclASTNode;

import java.util.*;
import java.util.List;

public class LabelNames extends SymbolTableVisitor<String> {
    public LabelNames(@NotNull ASTNode node, @NotNull VarNameGenerator gen) {
        super(node, gen);
    }
    protected @NotNull List<String> getAll(@NotNull ASTNode n) {
        return new ArrayList<>(n.accept(new GetLabels()));
    }
    private static class GetLabels implements ASTMonoidVisitor<Set<String>> {
        @Override
        public Set<String> empty() {
            return Collections.emptySet();
        }

        @Override
        public Set<String> compose(Set<String> o1, Set<String> o2) {
            Set<String> newSet = new HashSet<>(o1);
            newSet.addAll(o2);
            return newSet;
        }

        @Override
        public Set<String> visit(LabelDeclASTNode n) {
            return Set.of(n.getName());
        }
    }
}
