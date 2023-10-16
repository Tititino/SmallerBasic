package smallerbasic.AST.nodes;

import java.util.List;
import java.util.Optional;

public class ForLoopASTNode implements StatementASTNode {
    private final IdentifierASTNode varName;
    private final ExpressionASTNode start;
    private final ExpressionASTNode end;
    private final Optional<ExpressionASTNode> step;

    private final List<StatementASTNode> body;

    public ForLoopASTNode(IdentifierASTNode varName
            , ExpressionASTNode start
            , ExpressionASTNode end
            , ExpressionASTNode step
            , List<StatementASTNode> body) {
        this.varName = varName;
        this.start = start;
        this.end = end;
        this.step = Optional.of(step);
        this.body = body;
    }


    public ForLoopASTNode(IdentifierASTNode varName
            , ExpressionASTNode start
            , ExpressionASTNode end
            , List<StatementASTNode> body) {
        this.varName = varName;
        this.start = start;
        this.end = end;
        this.step = Optional.empty();
        this.body = body;
    }
}
