package smallerbasic.AST;

public class NumberLiteralASTNode implements LiteralASTNode {
    private final double value;

    public NumberLiteralASTNode(double value) {
        this.value = value;
    }
}
