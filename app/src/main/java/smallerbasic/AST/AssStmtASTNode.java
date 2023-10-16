package smallerbasic.AST;

import java.util.Objects;

public class AssStmtASTNode implements StatementASTNode {
    private final VariableASTNode varName;
    private final ExpressionASTNode value;

    public AssStmtASTNode(VariableASTNode varName, ExpressionASTNode value) {
        Objects.requireNonNull(varName);
        Objects.requireNonNull(value);
        this.varName = varName;
        this.value = value;
    }
}
