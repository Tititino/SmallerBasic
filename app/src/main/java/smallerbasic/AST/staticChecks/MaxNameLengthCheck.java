package smallerbasic.AST.staticChecks;

import org.antlr.v4.runtime.Token;
import org.jetbrains.annotations.NotNull;
import smallerbasic.AST.ASTMonoidVisitor;
import smallerbasic.AST.nodes.ASTNode;
import smallerbasic.AST.nodes.IdentifierASTNode;
import smallerbasic.AST.nodes.LabelNameASTNode;
import smallerbasic.AST.nodes.RoutineNameASTNode;

public class MaxNameLengthCheck extends AbstractCheck {

    private boolean isOk = true;
    @Override
    public boolean check(@NotNull ASTNode n) {
        isOk = true;
        n.accept(new NameGatherVisitor());
        return isOk;
    }

    @Override
    public void reportError(@NotNull ASTNode n, @NotNull String msg) {
        isOk = false;
        super.reportError(n, msg);
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

        private String pos(ASTNode n) {
            if (n.getStartToken().isPresent() && n.getEndToken().isPresent()) {
                Token start = n.getStartToken().get();
                Token end   = n.getEndToken().get();
                return " at line " + start.getLine() + ":" + start.getCharPositionInLine() + "-"
                        + (end.getCharPositionInLine() + end.getText().length() - 1);
            }
            return "";
        }

        @Override
        public Void visit(LabelNameASTNode n) {
            if (n.getText().length() > MAX_LEN)
                reportError(n, "*** NameMaxLenError: label \"" + n.getText() + "\""
                        + pos(n) + " exceeds max length (" + MAX_LEN + ")");
            return null;
        }

        @Override
        public Void visit(RoutineNameASTNode n) {
            if (n.getText().length() > MAX_LEN)
                reportError(n, "*** NameMaxLenError: routine name \"" + n.getText() + "\""
                        + pos(n) + " exceeds max length (" + MAX_LEN + ")");
            return null;
        }

        @Override
        public Void visit(IdentifierASTNode n) {
            if (n.getName().length() > MAX_LEN)
                reportError(n, "*** NameMaxLenError: variable name \"" + n.getName() + "\""
                        + pos(n) + " exceeds max length (" + MAX_LEN + ")");
            return null;
        }
    }
}
