package smallerbasic.AST.nodes;

import org.jetbrains.annotations.NotNull;
import smallerbasic.AST.ASTVisitor;

import java.util.Objects;

/**
 * An {@link ASTNode} representing a boolean literal.
 */
public class BoolLiteralASTNode extends AbstractASTNode implements LiteralASTNode {
    private final boolean value;

    public static @NotNull BoolLiteralASTNode parse(@NotNull String text) {
        return new BoolLiteralASTNode("true".equals(text));
    }
    public BoolLiteralASTNode(boolean value) {
        this.value = value;
    }

    public boolean getValue() {
        return value;
    }

    @Override
    public <T> T accept(ASTVisitor<T> v) {
        return v.visit(this);
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BoolLiteralASTNode that = (BoolLiteralASTNode) o;
        return value == that.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "BoolLiteralASTNode{" +
                "value=" + value +
                '}';
    }
}
