package smallerbasic.AST.nodes;

import org.jetbrains.annotations.NotNull;
import smallerbasic.AST.ASTVisitor;
import smallerbasic.AST.Scope;
import smallerbasic.symbolTable.HasSymbol;

import java.util.Objects;
/**
 * An {@link ASTNode} representing a name of a label.
 */
public class LabelNameASTNode extends AbstractASTNode implements ASTNode, HasSymbol {

    private final @NotNull String text;

    private final @NotNull Scope scope;

    public LabelNameASTNode(@NotNull String text, @NotNull Scope scope) {
        this.text = text;
        this.scope = scope;
    }

    public @NotNull Scope getScope() {
        return scope;
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
        LabelNameASTNode that = (LabelNameASTNode) o;
        return text.equals(that.text) && scope.equals(that.scope);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text, scope);
    }

    @Override
    public String toString() {
        return "LabelNameASTNode{" +
                "text='" + text + '\'' +
                ", scope=" + scope +
                '}';
    }
}
