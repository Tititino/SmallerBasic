package smallerbasic.AST.nodes;

import org.jetbrains.annotations.NotNull;
import smallerbasic.AST.ASTVisitor;

import java.util.Objects;

/**
 * An {@link ASTNode} representing a binary operation.
 * The binary operation is composed of a left {@link ExpressionASTNode}, a binary operator, and a
 * right {@link ExpressionASTNode}.
 */
public class BinOpASTNode extends AbstractASTNode implements ExpressionASTNode {

    private final @NotNull BinOp op;
    private final @NotNull ExpressionASTNode left;
    private final @NotNull ExpressionASTNode right;

    public BinOpASTNode(@NotNull BinOp op, @NotNull ExpressionASTNode left, @NotNull ExpressionASTNode right) {
        this.op = op;
        this.left = left;
        this.right = right;
    }

    @Override
    public <T> T accept(ASTVisitor<T> v) {
        return v.visit(this);
    }

    /**
     * The different possible binary operations in a SmallerBasic program.
     * No concat operation is defined since the choice between addition and concatenation is made at runtime.
     */
    public enum BinOp {
        PLUS,
        MINUS,
        MULT,
        DIV,
        GEQ,
        LEQ,
        EQ,
        LT,
        GT,
        NEQ,
        AND,
        OR;

        public static BinOp parse(@NotNull String s) {
            return switch (s) {
                case "+" -> PLUS;
                case "-" -> MINUS;
                case "*" -> MULT;
                case "/" -> DIV;
                case ">=" -> GEQ;
                case "<=" -> LEQ;
                case "=" -> EQ;
                case "<" -> LT;
                case ">" -> GT;
                case "<>" -> NEQ;
                case "And" -> AND;
                case "Or" -> OR;
                default -> throw new IllegalArgumentException("The string \"" + s + "\" is no a valid operator");
            };
        }
    }
    public @NotNull BinOp getOp() {
        return op;
    }

    public @NotNull ExpressionASTNode getLeft() {
        return left;
    }

    public @NotNull ExpressionASTNode getRight() {
        return right;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BinOpASTNode that = (BinOpASTNode) o;
        return op == that.op && left.equals(that.left) && right.equals(that.right);
    }

    @Override
    public int hashCode() {
        return Objects.hash(op, left, right);
    }

    @Override
    public String toString() {
        return "BinOpASTNode{" +
                "op=" + op +
                ", left=" + left +
                ", right=" + right +
                '}';
    }
}
