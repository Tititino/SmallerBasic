package smallerbasic.AST.nodes;

import org.jetbrains.annotations.NotNull;
import smallerbasic.AST.ASTVisitor;
import smallerbasic.symbolTable.HasSymbol;

import java.util.Objects;

/**
 * An {@link ASTNode} representing a unary minus.
 */
public class UnaryMinusASTNode extends AbstractASTNode implements ExpressionASTNode, HasSymbol {

    private final @NotNull ExpressionASTNode expr;

    public UnaryMinusASTNode(@NotNull ExpressionASTNode expr) {
        this.expr = expr;
    }

    public @NotNull ExpressionASTNode getExpr() {
        return expr;
    }

    @Override
    public <T> T accept(ASTVisitor<T> v) {
        return v.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UnaryMinusASTNode that = (UnaryMinusASTNode) o;
        return expr.equals(that.expr);
    }

    @Override
    public int hashCode() {
        return Objects.hash(expr);
    }

    @Override
    public String toString() {
        return "UnaryMinusASTNode{" +
                "expr=" + expr +
                '}';
    }
}
