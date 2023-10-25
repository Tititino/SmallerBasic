package smallerbasic.AST.nodes;

import org.jetbrains.annotations.NotNull;
import smallerbasic.AST.ASTVisitor;

import java.util.Objects;

public class ArrayASTNode extends AbstractASTNode implements ExpressionASTNode, VariableASTNode {
    private final @NotNull IdentifierASTNode name;
    private final @NotNull ExpressionASTNode index;

    public ArrayASTNode(@NotNull IdentifierASTNode name, @NotNull ExpressionASTNode index) {
        this.name = name;
        this.index = index;
    }

    public @NotNull IdentifierASTNode getName() {
        return name;
    }

    public @NotNull ExpressionASTNode getIndex() {
        return index;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArrayASTNode that = (ArrayASTNode) o;
        return name.equals(that.name) && index.equals(that.index);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, index);
    }

    @Override
    public String toString() {
        return "ArrayASTNode{" +
                "name=" + name +
                ", index=" + index +
                '}';
    }

    @Override
    public <T> T accept(ASTVisitor<T> v) {
        return v.visit(this);
    }
}
