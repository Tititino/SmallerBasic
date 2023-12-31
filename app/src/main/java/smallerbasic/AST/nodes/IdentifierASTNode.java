package smallerbasic.AST.nodes;

import org.jetbrains.annotations.NotNull;
import smallerbasic.AST.ASTVisitor;

import java.util.Objects;

/**
 * An {@link ASTNode} representing an identifier.
 */
public class IdentifierASTNode extends AbstractASTNode implements ExpressionASTNode, VariableASTNode {
    private final @NotNull String name;

    public IdentifierASTNode(@NotNull String name) {
        this.name = name;
    }

    public @NotNull String getName() {
        return name;
    }

    @Override
    public <T> T accept(ASTVisitor<T> v) {
        return v.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IdentifierASTNode that = (IdentifierASTNode) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "IdentifierASTNode{" +
                "name='" + name + '\'' +
                '}';
    }
}
