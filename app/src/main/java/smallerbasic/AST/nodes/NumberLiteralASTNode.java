package smallerbasic.AST.nodes;

import java.util.Objects;

public class NumberLiteralASTNode implements LiteralASTNode {
    private final double value;

    public static NumberLiteralASTNode parse(String text) {
        return new NumberLiteralASTNode(Double.parseDouble(text));
    }
    public NumberLiteralASTNode(double value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NumberLiteralASTNode that = (NumberLiteralASTNode) o;
        return Double.compare(that.value, value) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "NumberLiteralASTNode{" +
                "value=" + value +
                '}';
    }
}
