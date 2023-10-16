package smallerbasic.AST;

public class GotoStmtASTNode implements StatementASTNode {
    private final String label;

    public GotoStmtASTNode(String label) {
        this.label = label;
    }
}
