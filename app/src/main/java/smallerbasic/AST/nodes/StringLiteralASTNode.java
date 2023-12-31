package smallerbasic.AST.nodes;

import org.jetbrains.annotations.NotNull;
import smallerbasic.AST.ASTVisitor;

import java.util.Objects;

/**
 * An {@link ASTNode} representing a string literal.
 */
public class StringLiteralASTNode extends AbstractASTNode implements LiteralASTNode {
    private final @NotNull String value;

    public StringLiteralASTNode(@NotNull String value) {
        this.value = value;
    }

    public @NotNull String getValue() {
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
