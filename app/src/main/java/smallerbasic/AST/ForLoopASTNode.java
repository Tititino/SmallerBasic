package smallerbasic.AST;

import java.util.Optional;

public class ForLoopASTNode implements StatementASTNode {
    private final VariableASTNode varName;
    private final ExpressionASTNode start;
    private final ExpressionASTNode end;
    private final Optional<ExpressionASTNode> step;

    public ForLoopASTNode(VariableASTNode varName
            , ExpressionASTNode start
            , ExpressionASTNode end
            , ExpressionASTNode step) {
        this.varName = varName;
        this.start = start;
        this.end = end;
        this.step = Optional.of(step);
    }


    public ForLoopASTNode(VariableASTNode varName
            , ExpressionASTNode start
            , ExpressionASTNode end) {
        this.varName = varName;
        this.start = start;
        this.end = end;
        this.step = Optional.empty();
    }
}
