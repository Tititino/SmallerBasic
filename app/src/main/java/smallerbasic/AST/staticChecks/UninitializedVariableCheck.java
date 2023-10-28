package smallerbasic.AST.staticChecks;

import org.antlr.v4.runtime.Token;
import org.jetbrains.annotations.NotNull;
import smallerbasic.AST.ASTMonoidVisitor;
import smallerbasic.AST.nodes.*;

import java.util.*;

/**
 * This check verifies whether each variable has been initialized before its use.
 * It ignores completely array accesses as the indexes may depend on runtime values.
 */
public class UninitializedVariableCheck implements Check {

    private boolean isOk = true;
    @Override
    public boolean check(@NotNull ASTNode n) {
        isOk = true;
        n.accept(new UninitializedVisitor());
        return isOk;
    }

    @Override
    public void reportError(@NotNull String msg) {
        isOk = false;
        Check.super.reportError(msg);
    }

    private class UninitializedVisitor implements ASTMonoidVisitor<Set<IdentifierASTNode>> {

        private final @NotNull Set<IdentifierASTNode> setVars = new HashSet<>();

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
                    String pos = "";
                    if (i.getStartToken().isPresent()) {
                        Token start = i.getStartToken().get();
                        int startIndex = start.getCharPositionInLine();
                        pos = "used at " + start.getLine() + ":"
                                + startIndex + "-"
                                + (start.getText().length() + startIndex - 1) + " ";
                    }
                    reportError("*** UninitializedWarning: variable \""
                            + i.getName() + "\" " + pos + "may not have been initialized");
                    composed.remove(i);     // no new errors
                }
            return empty();
        }

        @Override
        public Set<IdentifierASTNode> visit(IdentifierASTNode n) {
            return Set.of(n);
        }

        @Override
        public Set<IdentifierASTNode> visit(ArrayASTNode n) {
            // does not make sense to check arrays since indexes could be not known statically
            visitChildren(n.getIndexes());
            return empty();
        }
    }

}
