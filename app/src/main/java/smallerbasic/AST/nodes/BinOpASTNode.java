package smallerbasic.AST.nodes;

import java.util.Objects;

public class BinOpASTNode implements ExpressionASTNode {

    private final BinOp op;
    private final ExpressionASTNode left;
    private final ExpressionASTNode right;

    public BinOpASTNode(BinOp op, ExpressionASTNode left, ExpressionASTNode right) {
        Objects.requireNonNull(op);
        Objects.requireNonNull(left);
        Objects.requireNonNull(right);
        this.op = op;
        this.left = left;
        this.right = right;
    }

    public enum BinOp {
        PLUS,
        MINUS,
        MULT,
        DIV,
        CONCAT,
        GEQ,
        LEQ,
        EQ,
        LT,
        GT,
        NEQ,
        AND,
        OR;

        public static BinOp parse(String s) {
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
