package smallerbasic.AST;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class IfThenASTNode implements StatementASTNode {
    private final ExpressionASTNode condition;
    private final List<StatementASTNode> trueBody;
    private final Optional<List<StatementASTNode>> falseBody;

    public IfThenASTNode(ExpressionASTNode condition, List<StatementASTNode> trueBody) {
        Objects.requireNonNull(condition);
        Objects.requireNonNull(trueBody);
        this.condition = condition;
        this.trueBody = trueBody;
        this.falseBody = Optional.empty();
    }

    public IfThenASTNode(ExpressionASTNode condition
            , List<StatementASTNode> trueBody
            , List<StatementASTNode> falseBody) {
        Objects.requireNonNull(condition);
        Objects.requireNonNull(trueBody);
        Objects.requireNonNull(falseBody);
        this.condition = condition;
        this.trueBody = trueBody;
        this.falseBody = Optional.of(falseBody);
    }
}
