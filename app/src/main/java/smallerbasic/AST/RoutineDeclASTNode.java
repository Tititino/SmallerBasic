package smallerbasic.AST;

import java.util.List;
import java.util.Objects;

public class RoutineDeclASTNode implements DeclOrStmtASTNode {
    private final String name;
    private final List<StatementASTNode> body;

    public RoutineDeclASTNode(String name, List<StatementASTNode> body) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(body);
        this.name = name;
        this.body = body;
    }
}
