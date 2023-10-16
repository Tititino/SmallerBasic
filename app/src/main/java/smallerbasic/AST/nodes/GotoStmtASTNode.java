package smallerbasic.AST.nodes;

public class GotoStmtASTNode implements StatementASTNode {
    private final String label;

    public GotoStmtASTNode(String label) {
        this.label = label;
    }
}
