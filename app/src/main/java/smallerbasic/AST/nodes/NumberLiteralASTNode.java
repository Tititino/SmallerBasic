package smallerbasic.AST.nodes;

import org.jetbrains.annotations.NotNull;
import smallerbasic.AST.ASTVisitor;

import java.util.Objects;

/**
 * An {@link ASTNode} representing a number literal.
 * A number is SmallerBasic is a double.
 */
public class NumberLiteralASTNode extends AbstractASTNode implements LiteralASTNode {
    private final double value;

    public static @NotNull NumberLiteralASTNode parse(@NotNull String text) {
        return new NumberLiteralASTNode(Double.parseDouble(text));
    }
    public NumberLiteralASTNode(double value) {
        this.value = value;
    }

    public double getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NumberLiteralASTNode that = (NumberLiteralASTNode) o;
        return Double.compare(that.value, value) == 0;
    }

    @Override
    public <T> T accept(ASTVisitor<T> v) {
        return v.visit(this);
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
