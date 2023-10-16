package smallerbasic.AST;

public class LabelDeclASTNode implements StatementASTNode {
    private final String name;

    public LabelDeclASTNode(String name) {
        this.name = name;
    }
}
