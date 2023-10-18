package smallerbasic.AST.nodes;

import smallerbasic.AST.ASTVisitor;

import java.util.Collections;
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

    public ExpressionASTNode getCondition() {
        return condition;
    }

    public List<StatementASTNode> getTrueBody() {
        return Collections.unmodifiableList(trueBody);
    }

    public Optional<List<StatementASTNode>> getFalseBody() {
        return falseBody.map(Collections::unmodifiableList);
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
        return condition.equals(that.condition) && trueBody.equals(that.trueBody) && falseBody.equals(that.falseBody);
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
