package smallerbasic.AST.nodes;

import smallerbasic.AST.ASTVisitor;

import java.util.Objects;

public class LabelDeclASTNode implements StatementASTNode {
    private final String name;

    public LabelDeclASTNode(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public <T> T accept(ASTVisitor<T> v) {
        return v.visit(this);
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LabelDeclASTNode that = (LabelDeclASTNode) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "LabelDeclASTNode{" +
                "name='" + name + '\'' +
                '}';
    }
}
