package smallerbasic.AST.nodes;

import org.jetbrains.annotations.NotNull;
import smallerbasic.AST.ASTVisitor;

import java.util.Objects;

public class AssStmtASTNode extends AbstractASTNode implements StatementASTNode {
    private final @NotNull IdentifierASTNode varName;
    private final @NotNull ExpressionASTNode value;

    public AssStmtASTNode(@NotNull IdentifierASTNode varName, @NotNull ExpressionASTNode value) {
        this.varName = varName;
        this.value = value;
    }

    public @NotNull IdentifierASTNode getVarName() {
        return varName;
    }

    public @NotNull ExpressionASTNode getValue() {
        return value;
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

    @Override
    public <T> T accept(ASTVisitor<T> v) {
        return v.visit(this);
    }
}
