package smallerbasic.AST;

public class VariableASTNode implements ExpressionASTNode {
    private final String name;

    public VariableASTNode(String name) {
        this.name = name;
    }
}
