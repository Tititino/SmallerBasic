package smallerbasic.AST;

public interface ASTVisitable {
    <T> T accept(ASTVisitor<T> v);
}
