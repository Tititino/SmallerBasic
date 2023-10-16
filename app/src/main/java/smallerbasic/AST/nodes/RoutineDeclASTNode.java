package smallerbasic.AST.nodes;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoutineDeclASTNode that = (RoutineDeclASTNode) o;
        return name.equals(that.name) && body.equals(that.body);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, body);
    }

    @Override
    public String toString() {
        return "RoutineDeclASTNode{" +
                "name='" + name + '\'' +
                ", body=" + body +
                '}';
    }
}
