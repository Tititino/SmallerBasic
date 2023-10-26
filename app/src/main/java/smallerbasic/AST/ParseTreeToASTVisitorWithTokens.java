package smallerbasic.AST;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.jetbrains.annotations.NotNull;
import smallerbasic.AST.nodes.*;

public class ParseTreeToASTVisitorWithTokens extends ParseTreeToASTVisitor {
    private <N extends ASTNode> @NotNull N setTokens(@NotNull N ast, @NotNull ParserRuleContext ctx) {
        ast.setStartToken(ctx.getStart());
        ast.setEndToken(ctx.getStop());
        return ast;
    }

    // non sono troppo sicuro di questa cosa
    public @NotNull ASTNode visit(@NotNull ParseTree tree) {
        return setTokens(tree.accept(this), (ParserRuleContext) tree.getPayload());
    }

}