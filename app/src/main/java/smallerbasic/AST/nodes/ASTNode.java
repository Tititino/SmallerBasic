package smallerbasic.AST.nodes;

import org.antlr.v4.runtime.Token;
import org.jetbrains.annotations.NotNull;
import smallerbasic.AST.ASTVisitable;

import java.util.Optional;


/**
 * A node of the AST.
 */
public interface ASTNode extends ASTVisitable {
    /**
     * Set the starting token of this node.
     * @param token The starting token of this node. Should not be {@code null}.
     */
    void setStartToken(@NotNull Token token);
    /**
     * Set the last token of this node.
     * @param token The last token of this node. Should not be {@code null}.
     */
    void setEndToken(@NotNull Token token);

    /**
     * Retrieve the starting token associated with this node.
     * @return {@code Optional.empty()} if no token are associated with this node, otherwise the starting token.
     */
    @NotNull Optional<@NotNull Token> getStartToken();
    /**
     * Retrieve the last token associated with this node.
     * @return {@code Optional.empty()} if no token are associated with this node, otherwise the token.
     * If the node is a terminal one (e.g. {@link IdentifierASTNode}) the end token should be the same as the start one.
     */
    @NotNull Optional<@NotNull Token> getEndToken();

}
