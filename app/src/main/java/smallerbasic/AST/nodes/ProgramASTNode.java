package smallerbasic.AST.nodes;

import org.jetbrains.annotations.NotNull;
import smallerbasic.AST.ASTVisitor;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * An {@link ASTNode} representing a program.
 * A program is made out of either routine declarations
 * ({@link RoutineDeclASTNode}) or statements ({@link StatementASTNode}).
 */
public class ProgramASTNode extends AbstractASTNode implements ASTNode {

    private final @NotNull List<@NotNull DeclOrStmtASTNode> contents;

    public ProgramASTNode(@NotNull List<@NotNull DeclOrStmtASTNode> contents) {
        this.contents = contents;
    }

    public @NotNull List<@NotNull DeclOrStmtASTNode> getContents() {
        return Collections.unmodifiableList(contents);
    }

    @Override
    public <T> T accept(ASTVisitor<T> v) {
        return v.visit(this);
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
