package smallerbasic.AST.nodes;

import java.util.List;

public class WhileLoopASTNode implements StatementASTNode {
    private final ExpressionASTNode condition;
    private final List<StatementASTNode> body;

    public WhileLoopASTNode(ExpressionASTNode condition, List<StatementASTNode> body) {
        this.condition = condition;
        this.body = body;
    }
}
