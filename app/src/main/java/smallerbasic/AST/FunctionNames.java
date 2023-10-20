package smallerbasic.AST;

import org.jetbrains.annotations.NotNull;
import smallerbasic.AST.nodes.ASTNode;
import smallerbasic.AST.nodes.RoutineDeclASTNode;

import java.util.*;

public class FunctionNames extends SymbolTableVisitor<String> {
    public FunctionNames(@NotNull ASTNode node, @NotNull VarNameGenerator gen) {
        super(node, gen);
    }
    protected @NotNull List<String> getAll(@NotNull ASTNode n) {
        return new ArrayList<>(n.accept(new GetFuncs()));
    }
    private static class GetFuncs implements ASTMonoidVisitor<Set<String>> {
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
        public Set<String> visit(RoutineDeclASTNode n) {
            return Set.of(n.getName());
        }
    }
}
