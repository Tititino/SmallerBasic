package smallerbasic.AST.nodes;

import java.util.List;
import java.util.Objects;

public class RoutineCallASTNode implements StatementASTNode {
    private final String module;
    private final String function;
    private final List<ExpressionASTNode> args;

    public RoutineCallASTNode(String module, String function, List<ExpressionASTNode> args) {
        Objects.requireNonNull(module);
        Objects.requireNonNull(function);
        Objects.requireNonNull(args);
        this.module = module;
        this.function = function;
        this.args = args;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoutineCallASTNode that = (RoutineCallASTNode) o;
        return module.equals(that.module) && function.equals(that.function) && args.equals(that.args);
    }

    @Override
    public int hashCode() {
        return Objects.hash(module, function, args);
    }

    @Override
    public String toString() {
        return "RoutineCallASTNode{" +
                "module='" + module + '\'' +
                ", function='" + function + '\'' +
                ", args=" + args +
                '}';
    }
}
