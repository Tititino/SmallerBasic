package smallerbasic.AST.nodes;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import smallerbasic.AST.ASTVisitor;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class IfThenASTNode extends AbstractASTNode implements StatementASTNode {
    private final @NotNull ExpressionASTNode condition;
    private final @NotNull List<@NotNull StatementASTNode> trueBody;
    private final @Nullable List<@NotNull StatementASTNode> falseBody;

    public IfThenASTNode(
            @NotNull ExpressionASTNode condition,
            @NotNull List<@NotNull StatementASTNode> trueBody) {
        this(condition, trueBody, null);
    }

    public IfThenASTNode(
            @NotNull ExpressionASTNode condition,
            @NotNull List<@NotNull StatementASTNode> trueBody,
            @Nullable List<@NotNull StatementASTNode> falseBody) {
        this.condition = condition;
        this.trueBody = trueBody;
        this.falseBody = falseBody;
    }

    public @NotNull ExpressionASTNode getCondition() {
        return condition;
    }

    public @NotNull List<@NotNull StatementASTNode> getTrueBody() {
        return Collections.unmodifiableList(trueBody);
    }

    public @NotNull Optional<@NotNull List<@NotNull StatementASTNode>> getFalseBody() {
        return Optional.ofNullable(falseBody).map(Collections::unmodifiableList);
    }

    @Override
    public <T> T accept(ASTVisitor<T> v) {
        return v.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IfThenASTNode that = (IfThenASTNode) o;
        return condition.equals(that.condition)
                && trueBody.equals(that.trueBody)
                && Objects.equals(falseBody, that.falseBody);
    }

    @Override
    public int hashCode() {
        return Objects.hash(condition, trueBody, falseBody);
    }

    @Override
    public String toString() {
        return "IfThenASTNode{" +
                "condition=" + condition +
                ", trueBody=" + trueBody +
                ", falseBody=" + falseBody +
                '}';
    }


}
