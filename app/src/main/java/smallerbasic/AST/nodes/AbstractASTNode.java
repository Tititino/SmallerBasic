package smallerbasic.AST.nodes;

import org.antlr.v4.runtime.Token;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import smallerbasic.AST.SymbolTableVisitor;
import smallerbasic.AST.VarNameGenerator;
import smallerbasic.SymbolTable;

import java.util.Optional;

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
