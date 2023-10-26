package smallerbasic.AST.nodes;

import org.antlr.v4.runtime.Token;
import org.jetbrains.annotations.NotNull;
import smallerbasic.AST.ASTVisitable;

import java.util.Optional;


public interface ASTNode extends ASTVisitable {
    /**
     * A node may be associated with a start and end token.
     */
    void setStartToken(@NotNull Token token);
    void setEndToken(@NotNull Token token);

    /**
     * These methods retrieve the starting and ending token associated with this node.
     * @return empty() if no token are associated with this node, the token otherwise.
     */
    @NotNull Optional<@NotNull Token> getStartToken();
    @NotNull Optional<@NotNull Token> getEndToken();

}
