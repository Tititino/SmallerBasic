package smallerbasic.AST;

import java.util.List;
import java.util.Objects;

public class ProgramASTNode {

    private final List<DeclOrStmtASTNode> contents;

    public ProgramASTNode(List<DeclOrStmtASTNode> contents) {
        Objects.requireNonNull(contents);
        this.contents = contents;
    }
}
