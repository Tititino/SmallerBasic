package smallerbasic.AST.nodes;

import org.jetbrains.annotations.NotNull;
import smallerbasic.AST.ASTVisitor;

import java.util.Objects;

/**
 * An {@link ASTNode} representing a call to a subroutine.
 */
public class RoutineCallASTNode extends AbstractASTNode implements StatementASTNode {
    private final @NotNull RoutineNameASTNode function;
    public RoutineCallASTNode(@NotNull RoutineNameASTNode function) {
        this.function = function;
    }

    public @NotNull RoutineNameASTNode getFunction() {
        return function;
    }

    @Override
    public <T> T accept(ASTVisitor<T> v) {
        return v.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoutineCallASTNode that = (RoutineCallASTNode) o;
        return function.equals(that.function);
    }

    @Override
    public int hashCode() {
        return Objects.hash(function);
    }

    @Override
    public String toString() {
        return "RoutineCallASTNode{" +
                "function='" + function + '\'' +
                '}';
    }
}
