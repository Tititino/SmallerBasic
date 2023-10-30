package smallerbasic.AST.nodes;


/**
 * An {@link ASTNode} representing either a routine declaration or a statement.
 * This interface groups together {@link StatementASTNode} and {@link RoutineDeclASTNode}
 * since a program is made out of one of these two.
 */
public interface DeclOrStmtASTNode extends ASTNode {}
