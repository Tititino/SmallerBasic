package smallerbasic.AST.nodes;

import java.util.List;
import java.util.Objects;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ForLoopASTNode that = (ForLoopASTNode) o;
        return varName.equals(that.varName) && start.equals(that.start) && end.equals(that.end) && step.equals(that.step) && body.equals(that.body);
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
