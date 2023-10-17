package smallerbasic.AST.nodes;

import smallerbasic.AST.ASTVisitor;

import java.util.List;
import java.util.Objects;

public class WhileLoopASTNode implements StatementASTNode {
    private final ExpressionASTNode condition;
    private final List<StatementASTNode> body;

    public WhileLoopASTNode(ExpressionASTNode condition, List<StatementASTNode> body) {
        this.condition = condition;
        this.body = body;
    }
    @Override
    public <T> T accept(ASTVisitor<T> v) {
        return v.visit(this);
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WhileLoopASTNode that = (WhileLoopASTNode) o;
        return condition.equals(that.condition) && body.equals(that.body);
    }

    @Override
    public int hashCode() {
        return Objects.hash(condition, body);
    }

    @Override
    public String toString() {
        return "WhileLoopASTNode{" +
                "condition=" + condition +
                ", body=" + body +
                '}';
    }
}
