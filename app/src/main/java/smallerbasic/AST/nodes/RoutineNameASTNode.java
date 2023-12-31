package smallerbasic.AST.nodes;

import org.jetbrains.annotations.NotNull;
import smallerbasic.AST.ASTVisitor;

import java.util.Objects;

/**
 * An {@link ASTNode} representing a name of a subroutine.
 */
public class RoutineNameASTNode extends AbstractASTNode implements ASTNode {

    private final @NotNull String text;

    public RoutineNameASTNode(@NotNull String text) {
        this.text = text;
    }

    public @NotNull String getText() {
        return text;
    }

    @Override
    public <T> T accept(ASTVisitor<T> v) {
        return v.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoutineNameASTNode that = (RoutineNameASTNode) o;
        return text.equals(that.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text);
    }

    @Override
    public String toString() {
        return "RoutineNameASTNode{" +
                "text='" + text + '\'' +
                '}';
    }
}
