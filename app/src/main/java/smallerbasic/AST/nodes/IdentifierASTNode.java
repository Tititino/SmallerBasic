package smallerbasic.AST.nodes;

public class IdentifierASTNode implements ExpressionASTNode {
    private final String name;

    public IdentifierASTNode(String name) {
        this.name = name;
    }
}
