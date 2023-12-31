package smallerbasic.AST.nodes;

import org.jetbrains.annotations.NotNull;
import smallerbasic.AST.ASTVisitor;

import java.util.Objects;

/**
 * An {@link ASTNode} representing a declaration of a label.
 */
public class LabelDeclASTNode extends AbstractASTNode implements StatementASTNode {
    private final @NotNull LabelNameASTNode name;

    public LabelDeclASTNode(@NotNull LabelNameASTNode name) {
        this.name = name;
    }

    public @NotNull LabelNameASTNode getName() {
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
        LabelDeclASTNode that = (LabelDeclASTNode) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "LabelDeclASTNode{" +
                "name='" + name + '\'' +
                '}';
    }

}
