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
}
