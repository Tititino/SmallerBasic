package smallerbasic.AST.nodes;

import java.util.Objects;

public class StringLiteralASTNode implements LiteralASTNode {
    private final String value;

    public StringLiteralASTNode(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StringLiteralASTNode that = (StringLiteralASTNode) o;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "StringLiteralASTNode{" +
                "value='" + value + '\'' +
                '}';
    }
}
