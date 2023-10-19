package smallerbasic.AST.nodes;

import org.jetbrains.annotations.NotNull;
import smallerbasic.AST.ASTVisitor;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class RoutineDeclASTNode extends AbstractASTNode implements DeclOrStmtASTNode {
    private final @NotNull String name;
    private final @NotNull List<@NotNull StatementASTNode> body;

    public RoutineDeclASTNode(@NotNull String name, @NotNull List<@NotNull StatementASTNode> body) {
        this.name = name;
        this.body = body;
    }

    public @NotNull String getName() {
        return name;
    }

    public @NotNull List<@NotNull StatementASTNode> getBody() {
        return Collections.unmodifiableList(body);
    }

    @Override
    public <T> T accept(ASTVisitor<T> v) {
        return v.visit(this);
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
