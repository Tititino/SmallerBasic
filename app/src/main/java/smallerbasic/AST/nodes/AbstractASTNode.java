package smallerbasic.AST.nodes;

import org.antlr.v4.runtime.Token;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Each node in the AST MAY come with a Token (or Tokens).
 * If a node is a leaf the start token and the end token will be the same.
 * The token information is NOT used to test if two nodes are equal, and only their fields are considered.
 */
public abstract class AbstractASTNode implements ASTNode {

    private @Nullable Token start = null;
    private @Nullable Token end   = null;
    @Override
    public void setStartToken(@NotNull Token token) {
        start = token;
    }
    @Override
    public void setEndToken(@NotNull Token token) {
        end = token;
    }

    @Override
    public @NotNull Optional<@NotNull Token> getStartToken() {
        return Optional.ofNullable(start);
    }

    @Override
    public @NotNull Optional<@NotNull Token> getEndToken() {
        return Optional.ofNullable(end);
    }

}
