package smallerbasic.AST.nodes;

import org.jetbrains.annotations.NotNull;
import smallerbasic.AST.ASTVisitor;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * An {@link ASTNode} representing an array access.
 * The array itself is identified by an {@link IdentifierASTNode} and the indexes are
 * determined by a series of {@link ExpressionASTNode}.
 */
public class ArrayASTNode extends AbstractASTNode implements ExpressionASTNode, VariableASTNode {
    private final @NotNull IdentifierASTNode name;
    private final @NotNull List<@NotNull ExpressionASTNode> indexes;

    public ArrayASTNode(@NotNull IdentifierASTNode name, @NotNull List<@NotNull ExpressionASTNode> indexes) {
        this.name = name;
        this.indexes = indexes;
    }

    public @NotNull IdentifierASTNode getName() {
        return name;
    }

    public @NotNull List<@NotNull ExpressionASTNode> getIndexes() {
        return Collections.unmodifiableList(indexes);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArrayASTNode that = (ArrayASTNode) o;
        return name.equals(that.name) && indexes.equals(that.indexes);
    }

    @Override
    public String toString() {
        return "ArrayASTNode{" +
                "name=" + name +
                ", index=" + indexes +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, indexes);
    }

    @Override
    public <T> T accept(ASTVisitor<T> v) {
        return v.visit(this);
    }
}
