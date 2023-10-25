package smallerbasic.AST.nodes;

import org.jetbrains.annotations.NotNull;
import smallerbasic.AST.ASTVisitor;
import smallerbasic.symbolTable.HasSymbol;

import java.util.Objects;

public class IdentifierASTNode extends AbstractASTNode implements ExpressionASTNode, VariableASTNode, HasSymbol {
    private final @NotNull String name;

    public IdentifierASTNode(@NotNull String name) {
        this.name = name;
    }

    @Override
    public <T> T accept(ASTVisitor<T> v) {
        return v.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IdentifierASTNode that = (IdentifierASTNode) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "IdentifierASTNode{" +
                "name='" + name + '\'' +
                '}';
    }
}
