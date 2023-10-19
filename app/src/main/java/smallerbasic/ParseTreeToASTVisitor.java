package smallerbasic;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.jetbrains.annotations.NotNull;
import smallerbasic.AST.nodes.*;

import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

class ParseTreeToASTVisitor implements SBGrammarVisitor<ASTNode>  {

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

    private @NotNull List<StatementASTNode> childrenToAST(@NotNull List<SBGrammarParser.StatementContext> ctxs) {
        return ctxs.stream()
                .map(x -> (StatementASTNode) setTokens(visit(x), x))
                .toList();
    }

    @Override
    public @NotNull ASTNode visitProgram(SBGrammarParser.@NotNull ProgramContext ctx) {
        List<DeclOrStmtASTNode> body = IntStream.range(0, ctx.getChildCount())
                .mapToObj(i ->
                    (DeclOrStmtASTNode) visit(ctx.getChild(i))
                ).toList();
        return setTokens(new ProgramASTNode(body), ctx);
    }

    @Override
    public @NotNull ASTNode visitAssignmentStmt(SBGrammarParser.@NotNull AssignmentStmtContext ctx) {
        IdentifierASTNode ident = new IdentifierASTNode(ctx.Ident(0).getText());

        TerminalNode ident2 = ctx.Ident(1);
        if (!Objects.isNull(ident2))
            return setTokens(
                    new AssStmtASTNode(
                            ident,
                            setToken(new IdentifierASTNode(ident2.getText()), ident2.getSymbol())
                    ),
                    ctx
            );

        return setTokens(new AssStmtASTNode(ident, (ExpressionASTNode) visit(ctx.expression())), ctx);
    }

    @Override
    public @NotNull ASTNode visitLabel(SBGrammarParser.@NotNull LabelContext ctx) {
        return setTokens(new LabelDeclASTNode(ctx.Ident().getText()), ctx);
    }

    @Override
    public @NotNull ASTNode visitIfStmt(SBGrammarParser.@NotNull IfStmtContext ctx) {
        ExpressionASTNode condition = (ExpressionASTNode) visit(ctx.cond);
        List<StatementASTNode> trueBody = childrenToAST(ctx.bodyTrue);
        if (ctx.bodyFalse.isEmpty())
            return new IfThenASTNode(condition, trueBody);
        else
            return new IfThenASTNode(condition, trueBody, childrenToAST(ctx.bodyFalse));
    }

    @Override
    public @NotNull ASTNode visitForStmt(SBGrammarParser.@NotNull ForStmtContext ctx) {
        IdentifierASTNode varName = setToken(new IdentifierASTNode(ctx.var.getText()), ctx.var);
        ExpressionASTNode from = (ExpressionASTNode) visit(ctx.from);
        ExpressionASTNode to   = (ExpressionASTNode) visit(ctx.to);
        List<StatementASTNode> body = childrenToAST(ctx.body);
        if (Objects.isNull(ctx.step))
            return setTokens(new ForLoopASTNode(varName, from, to, body), ctx);
        else
            return setTokens(
                    new ForLoopASTNode(varName, from, to,
                            (ExpressionASTNode) visit(ctx.step),
                            body
                    ),
                    ctx
            );
    }

    @Override
    public @NotNull ASTNode visitWhileStmt(SBGrammarParser.@NotNull WhileStmtContext ctx) {
        return setTokens(
                new WhileLoopASTNode(
                        (ExpressionASTNode) visit(ctx.cond),
                        childrenToAST(ctx.body)
                ),
                ctx
        );
    }

    @Override
    public @NotNull ASTNode visitGotoStmt(SBGrammarParser.@NotNull GotoStmtContext ctx) {
        return setTokens(new GotoStmtASTNode(ctx.Ident().getText()), ctx);
    }

    @Override
    public @NotNull ASTNode visitBParens(SBGrammarParser.@NotNull BParensContext ctx) {
        return visit(ctx.expr);
    }

    @Override
    public @NotNull ASTNode visitStringComparison(SBGrammarParser.@NotNull StringComparisonContext ctx) {
        return setTokens(
                new BinOpASTNode(
                        BinOpASTNode.BinOp.parse(ctx.relop.getText()),
                        (ExpressionASTNode) visit(ctx.left),
                        (ExpressionASTNode) visit(ctx.right)
                ),
                ctx
        );
    }

    @Override
    public @NotNull ASTNode visitBoolLiteral(SBGrammarParser.@NotNull BoolLiteralContext ctx) {
        return setTokens(
                BoolLiteralASTNode.parse(ctx.Bool().getText()),
                ctx
        );
    }

    @Override
    public ASTNode visitBoolReturningFunc(SBGrammarParser.BoolReturningFuncContext ctx) {
        return visitCallExternalFunction(ctx.callExternalFunction());
    }

    @Override
    public @NotNull ASTNode visitNumberComparison(SBGrammarParser.@NotNull NumberComparisonContext ctx) {
        return setTokens(
                new BinOpASTNode(
                        BinOpASTNode.BinOp.parse(ctx.relop.getText()),
                        (ExpressionASTNode) visit(ctx.left),
                        (ExpressionASTNode) visit(ctx.right)
                ),
                ctx
        );
    }

    @Override
    public @NotNull ASTNode visitBoolOp(SBGrammarParser.@NotNull BoolOpContext ctx) {
        return setTokens(
                new BinOpASTNode(
                        BinOpASTNode.BinOp.parse(ctx.binop.getText()),
                        (ExpressionASTNode) visit(ctx.left),
                        (ExpressionASTNode) visit(ctx.right)
                ),
                ctx
        );
    }

    @Override
    public @NotNull ASTNode visitBoolIdent(SBGrammarParser.@NotNull BoolIdentContext ctx) {
        return setTokens(new IdentifierASTNode(ctx.Ident().getText()), ctx);
    }

    @Override
    public @NotNull ASTNode visitStringConcat(SBGrammarParser.@NotNull StringConcatContext ctx) {
        return setTokens(
                new BinOpASTNode(
                        BinOpASTNode.BinOp.CONCAT,
                        (ExpressionASTNode) visit(ctx.left),
                        (ExpressionASTNode) visit(ctx.right)
                ),
                ctx
        );
    }

    @Override
    public ASTNode visitStrReturningFunc(SBGrammarParser.StrReturningFuncContext ctx) {
        return visit(ctx.callExternalFunction());
    }

    @Override
    public @NotNull ASTNode visitStringLiteral(SBGrammarParser.@NotNull StringLiteralContext ctx) {
        String str = ctx.getText();
        return setTokens(new StringLiteralASTNode(str.substring(1, str.length() - 1)), ctx);
    }

    @Override
    public @NotNull ASTNode visitSParens(SBGrammarParser.@NotNull SParensContext ctx) {
        return visit(ctx.expr);
    }

    @Override
    public @NotNull ASTNode visitStringIdent(SBGrammarParser.@NotNull StringIdentContext ctx) {
        return setTokens(new IdentifierASTNode(ctx.Ident().getText()), ctx);
    }

    @Override
    public @NotNull ASTNode visitNumberIdent(SBGrammarParser.@NotNull NumberIdentContext ctx) {
        return setTokens(new IdentifierASTNode(ctx.Ident().getText()), ctx);
    }

    @Override
    public @NotNull ASTNode visitNParens(SBGrammarParser.@NotNull NParensContext ctx) {
        return visit(ctx.expr);
    }

    @Override
    public @NotNull ASTNode visitDivMul(SBGrammarParser.@NotNull DivMulContext ctx) {
        return setTokens(
                new BinOpASTNode(
                        BinOpASTNode.BinOp.parse(ctx.op.getText()),
                        (ExpressionASTNode) visit(ctx.left),
                        (ExpressionASTNode) visit(ctx.right)
                ),
                ctx
        );
    }

    @Override
    public @NotNull ASTNode visitPlusMin(@NotNull SBGrammarParser.PlusMinContext ctx) {
        return setTokens(
                new BinOpASTNode(
                        BinOpASTNode.BinOp.parse(ctx.op.getText()),
                        (ExpressionASTNode) visit(ctx.left),
                        (ExpressionASTNode) visit(ctx.right)
                ),
                ctx
        );
    }

    @Override
    public ASTNode visitNumberReturningFunc(SBGrammarParser.NumberReturningFuncContext ctx) {
        return visit(ctx.callExternalFunction());
    }

    @Override
    public @NotNull ASTNode visitNumberLiteral(SBGrammarParser.@NotNull NumberLiteralContext ctx) {
        return setTokens(NumberLiteralASTNode.parse(ctx.Number().getText()), ctx);
    }

    @Override
    public @NotNull ASTNode visitCallRoutine(SBGrammarParser.@NotNull CallRoutineContext ctx) {
        String funcName = ctx.FunctionCall().getText();
        return setTokens(new RoutineCallASTNode(funcName.replaceAll("\\([\\t ]*\\)", "")), ctx);
    }

    @Override
    public ASTNode visitCallExternalFunction(SBGrammarParser.CallExternalFunctionContext ctx) {
        String[] funcCall = ctx.name.getText().split("\\.");
        return setTokens(
                new ExternalFunctionCallASTNode(
                        funcCall[0],
                        funcCall[1].substring(0, funcCall[1].length() - 1),
                        ctx.args.stream()
                                .map(x -> (ExpressionASTNode) visit(x))
                                .toList()
                ),
                ctx
        );
    }

    public @NotNull ASTNode visitStatement(SBGrammarParser.@NotNull StatementContext ctx) {
        return visit(ctx.getChild(0));
    }

    @Override
    public @NotNull ASTNode visitSubroutineDecl(SBGrammarParser.@NotNull SubroutineDeclContext ctx) {
        return setTokens(new RoutineDeclASTNode(ctx.name.getText(), childrenToAST(ctx.body)), ctx);
    }

    @Override
    public @NotNull ASTNode visitExpression(SBGrammarParser.@NotNull ExpressionContext ctx) {
        return visit(ctx.getChild(0));
    }

    public @NotNull ASTNode visitBooleanExpression(SBGrammarParser.@NotNull BooleanExpressionContext ctx) {
        return visit(ctx);
    }

    @Override
    public ASTNode visit(ParseTree tree) {
        return tree.accept(this);
    }

    @Override
    public ASTNode visitChildren(RuleNode node) {
        return null;
    }

    @Override
    public ASTNode visitTerminal(TerminalNode node) {
        return null;
    }

    @Override
    public ASTNode visitErrorNode(ErrorNode node) {
        return null;
    }
}
