package smallerbasic.AST.nodes;

import org.jetbrains.annotations.NotNull;
import smallerbasic.AST.ASTVisitor;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * An {@link ASTNode} representing a for loop.
 * A for loop is composed of a variable name, a start value, an end value, a step and a body.
 * If the step is not specified this is assumed to be 1.
 * The body may be empty.
 */
public class ForLoopASTNode extends AbstractASTNode implements StatementASTNode {
    private final @NotNull VariableASTNode varName;
    private final @NotNull ExpressionASTNode start;
    private final @NotNull ExpressionASTNode end;
    private final @NotNull ExpressionASTNode step;
    private final @NotNull List<@NotNull StatementASTNode> body;

    public ForLoopASTNode(
            @NotNull VariableASTNode varName,
            @NotNull ExpressionASTNode start,
            @NotNull ExpressionASTNode end,
            @NotNull  ExpressionASTNode step,
            @NotNull List<@NotNull StatementASTNode> body) {
        this.varName = varName;
        this.start = start;
        this.end = end;
        this.step = step;
        this.body = body;
    }


    public ForLoopASTNode(
            @NotNull VariableASTNode varName,
            @NotNull ExpressionASTNode start,
            @NotNull ExpressionASTNode end,
            @NotNull List<@NotNull StatementASTNode> body) {
        this(varName, start, end, new NumberLiteralASTNode(1.0), body);
    }

    @Override
    public <T> T accept(ASTVisitor<T> v) {
        return v.visit(this);
    }

    public @NotNull VariableASTNode getVarName() {
        return varName;
    }

    public @NotNull ExpressionASTNode getStart() {
        return start;
    }

    public @NotNull ExpressionASTNode getEnd() {
        return end;
    }

    public @NotNull ExpressionASTNode getStep() {
        return step;
    }

    public @NotNull List<@NotNull StatementASTNode> getBody() {
        return Collections.unmodifiableList(this.body);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ForLoopASTNode that = (ForLoopASTNode) o;
        return varName.equals(that.varName)
                && start.equals(that.start)
                && end.equals(that.end)
                && step.equals(that.step)
                && body.equals(that.body);
    }

    @Override
    public int hashCode() {
        return Objects.hash(varName, start, end, step, body);
    }

    @Override
    public String toString() {
        return "ForLoopASTNode{" +
                "varName=" + varName +
                ", start=" + start +
                ", end=" + end +
                ", step=" + step +
                ", body=" + body +
                '}';
    }

}
