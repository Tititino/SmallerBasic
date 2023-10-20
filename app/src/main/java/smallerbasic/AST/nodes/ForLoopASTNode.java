package smallerbasic.AST.nodes;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import smallerbasic.AST.ASTVisitor;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ForLoopASTNode extends AbstractASTNode implements StatementASTNode {
    private final @NotNull IdentifierASTNode varName;
    private final @NotNull ExpressionASTNode start;
    private final @NotNull ExpressionASTNode end;
    private final @Nullable ExpressionASTNode step;
    private final @NotNull List<@NotNull StatementASTNode> body;

    public ForLoopASTNode(
            @NotNull IdentifierASTNode varName,
            @NotNull ExpressionASTNode start,
            @NotNull ExpressionASTNode end,
            @Nullable  ExpressionASTNode step,
            @NotNull List<@NotNull StatementASTNode> body) {
        this.varName = varName;
        this.start = start;
        this.end = end;
        this.step = step;
        this.body = body;
    }


    public ForLoopASTNode(
            @NotNull IdentifierASTNode varName,
            @NotNull ExpressionASTNode start,
            @NotNull ExpressionASTNode end,
            @NotNull List<@NotNull StatementASTNode> body) {
        this(varName, start, end, null, body);
    }

    @Override
    public <T> T accept(ASTVisitor<T> v) {
        return v.visit(this);
    }

    public @NotNull IdentifierASTNode getVarName() {
        return varName;
    }

    public @NotNull ExpressionASTNode getStart() {
        return start;
    }

    public @NotNull ExpressionASTNode getEnd() {
        return end;
    }

    public @NotNull Optional<@NotNull ExpressionASTNode> getStep() {
        return Optional.ofNullable(step);
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
                && Objects.equals(step, that.step)
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
