package smallerbasic.AST.nodes;

public class BoolLiteralASTNode implements LiteralASTNode {
    private final boolean value;

    public BoolLiteralASTNode(boolean value) {
        this.value = value;
    }
}
