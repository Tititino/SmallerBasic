package smallerbasic.AST.nodes;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class RoutineCallASTNode implements StatementASTNode {
    private final @NotNull String function;
    public RoutineCallASTNode(@NotNull String function) {
        Objects.requireNonNull(function);
        this.function = function;
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
