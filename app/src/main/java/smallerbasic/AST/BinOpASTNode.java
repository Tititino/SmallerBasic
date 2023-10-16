package smallerbasic.AST;

import java.util.Objects;

public class BinOpASTNode {

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
        OR
    }
}
