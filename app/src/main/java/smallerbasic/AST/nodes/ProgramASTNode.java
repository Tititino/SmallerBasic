package smallerbasic.AST.nodes;

import java.util.List;
import java.util.Objects;

public class ProgramASTNode implements ASTNode {

    private final List<DeclOrStmtASTNode> contents;

    public ProgramASTNode(List<DeclOrStmtASTNode> contents) {
        Objects.requireNonNull(contents);
        this.contents = contents;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProgramASTNode that = (ProgramASTNode) o;
        return contents.equals(that.contents);
    }

    @Override
    public int hashCode() {
        return Objects.hash(contents);
    }

    @Override
    public String toString() {
        return "ProgramASTNode{" +
                "contents=" + contents +
                '}';
    }
}
