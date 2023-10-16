package smallerbasic.AST.nodes;

import java.util.Objects;

public class AssStmtASTNode implements StatementASTNode {
    private final IdentifierASTNode varName;
    private final ExpressionASTNode value;

    public AssStmtASTNode(IdentifierASTNode varName, ExpressionASTNode value) {
        Objects.requireNonNull(varName);
        Objects.requireNonNull(value);
        this.varName = varName;
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AssStmtASTNode that = (AssStmtASTNode) o;
        return varName.equals(that.varName) && value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(varName, value);
    }

    @Override
    public String toString() {
        return "AssStmtASTNode{" +
                "varName=" + varName +
                ", value=" + value +
                '}';
    }
}
