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
    /**
     * Take an {@link ASTNode} and set its start and end tokens.
     * @param ast The node.
     * @param ctx The context from which the start and end tokens are taken.
     * @return {@code ast} with its start and end tokens set.
     */
    private <N extends ASTNode> @NotNull N setTokens(@NotNull N ast, @NotNull ParserRuleContext ctx) {
        ast.setStartToken(ctx.getStart());
        ast.setEndToken(ctx.getStop());
        return ast;
    }

    /**
     * Set the token of a terminal {@link ASTNode}.
     * @param ast The terminal {@link ASTNode}.
     * @param tok The corresponding token in the source file.
     * @return {@code ast} with its start and end tokens set to {@code tok}.
     */
    private <N extends ASTNode> @NotNull N setToken(@NotNull N ast, @NotNull Token tok) {
        ast.setStartToken(tok);
        ast.setEndToken(tok);
        return ast;
    }

    /**
     * The payload of a {@link ParseTree} may be either a {@link org.antlr.v4.runtime.RuleContext}
     * or a {@link Token} (see {@link ParseTree#getPayload()}).
     */
    public @NotNull ASTNode visit(@NotNull ParseTree tree) {
        if (tree.getPayload() instanceof ParserRuleContext ctx)
            return setTokens(tree.accept(this), ctx);
        return setToken(tree.accept(this), (Token) tree.getPayload());
    }

}