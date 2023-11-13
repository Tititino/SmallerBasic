package smallerbasic.AST.staticChecks;

import org.jetbrains.annotations.NotNull;
import smallerbasic.AST.ASTMonoidVisitor;
import smallerbasic.AST.nodes.ASTNode;
import smallerbasic.AST.nodes.IdentifierASTNode;
import smallerbasic.AST.nodes.LabelNameASTNode;
import smallerbasic.AST.nodes.RoutineNameASTNode;

/**
 * Check for max identifier length.
 */
public class MaxNameLengthCheck extends AbstractCheck {

    private boolean isOk = true;
    @Override
    public boolean check(@NotNull ASTNode n) {
        isOk = true;
        n.accept(new NameGatherVisitor());
        return isOk;
    }

    private void reportError(@NotNull ASTNode n, @NotNull String msg) {
        isOk = false;
        super.reporter.reportError(n, msg);
    }

    private class NameGatherVisitor implements ASTMonoidVisitor<Void> {

        private static final int MAX_LEN = 40;

        @Override
        public Void empty() {
            return null;
        }

        @Override
        public Void compose(Void o1, Void o2) {
            return null;
        }

        @Override
        public Void visit(LabelNameASTNode n) {
            if (n.getText().length() > MAX_LEN)
                reportError(n, "*** NameMaxLenError: label \"" + n.getText() + "\" exceeds max length (" + MAX_LEN + ")");
            return null;
        }

        @Override
        public Void visit(RoutineNameASTNode n) {
            if (n.getText().length() > MAX_LEN)
                reportError(n, "*** NameMaxLenError: routine name \"" + n.getText() + "\" exceeds max length (" + MAX_LEN + ")");
            return null;
        }

        @Override
        public Void visit(IdentifierASTNode n) {
            if (n.getName().length() > MAX_LEN)
                reportError(n, "*** NameMaxLenError: variable name \"" + n.getName() + "\" exceeds max length (" + MAX_LEN + ")");
            return null;
        }
    }
}
