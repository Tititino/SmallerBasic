package smallerbasic.AST.nodes;

import smallerbasic.AST.ASTVisitor;

import java.util.Objects;

public class IdentifierASTNode implements ExpressionASTNode {
    private final String name;

    public IdentifierASTNode(String name) {
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
