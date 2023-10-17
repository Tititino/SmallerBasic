package smallerbasic.AST.nodes;

import smallerbasic.AST.ASTVisitor;


public interface ASTNode {
    <T> T accept(ASTVisitor<T> v);
}
