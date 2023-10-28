package smallerbasic.AST.staticChecks;

import org.jetbrains.annotations.NotNull;
import smallerbasic.AST.ASTMonoidVisitor;
import smallerbasic.AST.nodes.*;

import java.util.*;

public class UninitializedVariableCheck implements Check {

    @Override
    public boolean check(@NotNull ASTNode n) {
        return false;
    }

    private class UninitializedVisitor implements ASTMonoidVisitor<Set<IdentifierASTNode>> {

        private final @NotNull Set<IdentifierASTNode> setVars = new HashSet<>();

        @Override
        public Set<IdentifierASTNode> empty() {
            return null;
        }

        @Override
        public Set<IdentifierASTNode> compose(Set<IdentifierASTNode> o1, Set<IdentifierASTNode> o2) {
            Set<IdentifierASTNode> newSet = new HashSet<>(o1);
            newSet.addAll(o2);
            return newSet;
        }

        @Override
        public Set<IdentifierASTNode> visit(AssStmtASTNode n) {
            setVars.addAll(n.getVarName().accept(this));
            n.getValue().accept(this);
            return Collections.emptySet();
        }

        @Override
        public Set<IdentifierASTNode> visit(ForLoopASTNode n) {
            setVars.addAll(n.getVarName().accept(this));
            n.getStart().accept(this);
            n.getEnd().accept(this);
            n.getStep().accept(this);
            visitChildren(n.getBody());
            return empty();
        }

        @Override
        public Set<IdentifierASTNode> visit(BinOpASTNode n) {
            Set<IdentifierASTNode> usedLeft = n.getLeft().accept(this);
            Set<IdentifierASTNode> usedRight = n.getRight().accept(this);

            Set<IdentifierASTNode> composed = compose(usedLeft, usedRight);
            for (IdentifierASTNode i : composed)
                if (!setVars.contains(i)) {
                    reportError("this variable " + i + " may not have been initialized");
                    composed.remove(i);     // no new errors
                }
            return composed;
        }

        @Override
        public Set<IdentifierASTNode> visit(IdentifierASTNode n) {
            return Set.of(n);
        }
    }

}
