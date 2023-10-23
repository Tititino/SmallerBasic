package smallerbasic.AST.nodes;

import org.jetbrains.annotations.NotNull;
import smallerbasic.AST.ASTVisitor;

import java.util.Objects;

public class GotoStmtASTNode extends AbstractASTNode implements StatementASTNode {
    private final @NotNull LabelNameASTNode label;

    public GotoStmtASTNode(@NotNull LabelNameASTNode label) {
        this.label = label;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GotoStmtASTNode that = (GotoStmtASTNode) o;
        return label.equals(that.label);
    }

    public @NotNull LabelNameASTNode getLabel() {
        return label;
    }

    @Override
    public <T> T accept(ASTVisitor<T> v) {
        return v.visit(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(label);
    }

    @Override
    public String toString() {
        return "GotoStmtASTNode{" +
                "label='" + label + '\'' +
                '}';
    }


}
