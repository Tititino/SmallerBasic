package smallerbasic.AST.nodes;

import org.antlr.v4.runtime.Token;
import org.jetbrains.annotations.NotNull;
import smallerbasic.AST.ASTVisitor;

import java.util.Optional;


public interface ASTNode  {
    <T> T accept(ASTVisitor<T> v);
    void setStartToken(@NotNull Token token);
    void setEndToken(@NotNull Token token);
    @NotNull Optional<@NotNull Token> getStartToken();
    @NotNull Optional<@NotNull Token> getEndToken();

}
