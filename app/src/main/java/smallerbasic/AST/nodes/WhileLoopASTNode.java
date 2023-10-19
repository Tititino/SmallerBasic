package smallerbasic.AST.nodes;

import org.jetbrains.annotations.NotNull;
import smallerbasic.AST.ASTVisitor;

import java.util.List;
import java.util.Objects;

public class WhileLoopASTNode extends AbstractASTNode implements StatementASTNode {
    private final @NotNull ExpressionASTNode condition;
    private final @NotNull List<@NotNull StatementASTNode> body;

    public WhileLoopASTNode(@NotNull ExpressionASTNode condition, @NotNull List<StatementASTNode> body) {
        this.condition = condition;
        this.body = body;
    }
    @Override
    public <T> T accept(ASTVisitor<T> v) {
        return v.visit(this);
    }

    public @NotNull ExpressionASTNode getCondition() {
        return condition;
    }

    public @NotNull List<@NotNull StatementASTNode> getBody() {
        return body;
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
