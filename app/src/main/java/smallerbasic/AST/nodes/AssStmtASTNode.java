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
}
