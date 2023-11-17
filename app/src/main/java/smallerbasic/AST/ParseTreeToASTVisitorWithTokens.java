package smallerbasic.AST;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.jetbrains.annotations.NotNull;
import smallerbasic.AST.nodes.*;

/**
 * This class extends {@link ParseTreeToASTVisitor} by associating start and end tokens to each {@link ASTNode} created.
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

    /**
     * The payload of a {@link ParseTree} may be either a {@link org.antlr.v4.runtime.RuleContext}
     * or a {@link Token} ({@see ParseTree::getPayload()}).
     */
    public @NotNull ASTNode visit(@NotNull ParseTree tree) {
        if (tree.getPayload() instanceof ParserRuleContext ctx)
            return setTokens(tree.accept(this), ctx);
        return setToken(tree.accept(this), (Token) tree.getPayload());
    }

}