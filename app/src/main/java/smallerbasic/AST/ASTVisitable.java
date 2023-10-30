package smallerbasic.AST;

/**
 * Something that can be visited by an {@link ASTVisitor}.
 */
public interface ASTVisitable {
    <T> T accept(ASTVisitor<T> v);
}
