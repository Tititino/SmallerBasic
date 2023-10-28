package smallerbasic.AST;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.jetbrains.annotations.NotNull;
import smallerbasic.AST.nodes.*;

/**
 * A decorator over {@link ParseTreeToASTVisitor} that associates start and end token to each {@link ASTNode} created.
 */
public class ParseTreeToASTVisitorWithTokens extends ParseTreeToASTVisitor {
    private <N extends ASTNode> @NotNull N setTokens(@NotNull N ast, @NotNull ParserRuleContext ctx) {
        ast.setStartToken(ctx.getStart());
        ast.setEndToken(ctx.getStop());
        return ast;
    }

    private <N extends ASTNode> @NotNull N setToken(@NotNull N ast, @NotNull Token tok) {
        ast.setStartToken(tok);
        ast.setEndToken(tok);
        return ast;
    }

    // non sono troppo sicuro di questa cosa
    public @NotNull ASTNode visit(@NotNull ParseTree tree) {
        if (tree.getPayload() instanceof ParserRuleContext ctx)
            return setTokens(tree.accept(this), ctx);
        return setToken(tree.accept(this), (Token) tree.getPayload());
    }

}