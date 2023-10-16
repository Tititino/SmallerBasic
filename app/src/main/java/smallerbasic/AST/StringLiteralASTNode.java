package smallerbasic.AST;

public class StringLiteralASTNode implements LiteralASTNode {
    private final String value;

    public StringLiteralASTNode(String value) {
        this.value = value;
    }
}
