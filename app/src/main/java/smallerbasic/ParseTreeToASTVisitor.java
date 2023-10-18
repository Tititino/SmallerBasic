package smallerbasic;

import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.jetbrains.annotations.NotNull;
import smallerbasic.AST.nodes.*;

import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

/**
 * This will make heavy use of class casting
 */
class ParseTreeToASTVisitor implements SBGrammarVisitor<ASTNode>  {

    private @NotNull List<StatementASTNode> childrenToAST(@NotNull List<SBGrammarParser.StatementContext> ctxs) {
        return ctxs.stream()
                .map(x -> (StatementASTNode) visitStatement(x))
                .toList();
    }

    @Override
    public @NotNull ASTNode visitProgram(SBGrammarParser.@NotNull ProgramContext ctx) {
        List<DeclOrStmtASTNode> body = IntStream.range(0, ctx.getChildCount())
                .mapToObj(i -> {
                    if (ctx.getChild(i) instanceof SBGrammarParser.StatementContext stmt)
                        return (StatementASTNode) visitStatement(stmt);

                    if (ctx.getChild(i) instanceof SBGrammarParser.SubroutineDeclContext sub)
                        return (RoutineDeclASTNode) visitSubroutineDecl(sub);

                    throw new IllegalArgumentException(
                            "Unexpected or null program member (statement or subroutine declaration)"
                    );
                }).toList();
        return new ProgramASTNode(body);
    }

    @Override
    public @NotNull ASTNode visitAssignmentStmt(SBGrammarParser.@NotNull AssignmentStmtContext ctx) {
        IdentifierASTNode ident = new IdentifierASTNode(ctx.Ident(0).getText());

        SBGrammarParser.ExpressionContext expr = ctx.expression();
        if (!Objects.isNull(expr))
            return new AssStmtASTNode(ident, (ExpressionASTNode) visitExpression(expr));

        TerminalNode ident2 = ctx.Ident(1);
        if (!Objects.isNull(ident2))
            return new AssStmtASTNode(ident, new IdentifierASTNode(ident2.getText()));

        throw new IllegalArgumentException("Unexpected or null assignment");
    }

    @Override
    public @NotNull ASTNode visitLabel(SBGrammarParser.@NotNull LabelContext ctx) {
        return new LabelDeclASTNode(ctx.Ident().getText());
    }

    @Override
    public @NotNull ASTNode visitIfStmt(SBGrammarParser.@NotNull IfStmtContext ctx) {
        ExpressionASTNode condition = (ExpressionASTNode) visitBooleanExpression(ctx.cond);
        List<StatementASTNode> trueBody = childrenToAST(ctx.bodyTrue);
        if (ctx.bodyFalse.isEmpty())
            return new IfThenASTNode(condition, trueBody);
        else
            return new IfThenASTNode(condition, trueBody, childrenToAST(ctx.bodyFalse));
    }

    @Override
    public @NotNull ASTNode visitForStmt(SBGrammarParser.@NotNull ForStmtContext ctx) {
        IdentifierASTNode varName = new IdentifierASTNode(ctx.var.getText());
        ExpressionASTNode from = (ExpressionASTNode) visitArithExpression(ctx.from);
        ExpressionASTNode to   = (ExpressionASTNode) visitArithExpression(ctx.to);
        List<StatementASTNode> body = childrenToAST(ctx.body);
        if (Objects.isNull(ctx.step))
            return new ForLoopASTNode(varName, from, to, body);
        else
            return new ForLoopASTNode(varName, from, to,
                    (ExpressionASTNode) visitArithExpression(ctx.step),
                    body
            );
    }

    @Override
    public @NotNull ASTNode visitWhileStmt(SBGrammarParser.@NotNull WhileStmtContext ctx) {
        return new WhileLoopASTNode(
                (ExpressionASTNode) visitBooleanExpression(ctx.cond),
                childrenToAST(ctx.body)
        );
    }

    @Override
    public @NotNull ASTNode visitGotoStmt(SBGrammarParser.@NotNull GotoStmtContext ctx) {
        return new GotoStmtASTNode(ctx.Ident().getText());
    }

    @Override
    public @NotNull ASTNode visitBParens(SBGrammarParser.@NotNull BParensContext ctx) {
        return visitBooleanExpression(ctx.expr);
    }

    @Override
    public @NotNull ASTNode visitStringComparison(SBGrammarParser.@NotNull StringComparisonContext ctx) {
        return new BinOpASTNode(
                BinOpASTNode.BinOp.parse(ctx.relop.getText()),
                (ExpressionASTNode) visitStringExpression(ctx.left),
                (ExpressionASTNode) visitStringExpression(ctx.right)
        );
    }

    @Override
    public @NotNull ASTNode visitBoolLiteral(SBGrammarParser.@NotNull BoolLiteralContext ctx) {
        return BoolLiteralASTNode.parse(ctx.Bool().getText());
    }

    @Override
    public ASTNode visitBoolReturningFunc(SBGrammarParser.BoolReturningFuncContext ctx) {
        return visitCallExternalFunction(ctx.callExternalFunction());
    }

    @Override
    public @NotNull ASTNode visitNumberComparison(SBGrammarParser.@NotNull NumberComparisonContext ctx) {
        return new BinOpASTNode(
                BinOpASTNode.BinOp.parse(ctx.relop.getText()),
                (ExpressionASTNode) visitArithExpression(ctx.left),
                (ExpressionASTNode) visitArithExpression(ctx.right)
        );
    }

    @Override
    public @NotNull ASTNode visitBoolOp(SBGrammarParser.@NotNull BoolOpContext ctx) {
        return new BinOpASTNode(
                BinOpASTNode.BinOp.parse(ctx.binop.getText()),
                (ExpressionASTNode) visitBooleanExpression(ctx.left),
                (ExpressionASTNode) visitBooleanExpression(ctx.right)
        );
    }

    @Override
    public @NotNull ASTNode visitBoolIdent(SBGrammarParser.@NotNull BoolIdentContext ctx) {
        return new IdentifierASTNode(ctx.Ident().getText());
    }

    @Override
    public @NotNull ASTNode visitStringConcat(SBGrammarParser.@NotNull StringConcatContext ctx) {
        return new BinOpASTNode(
                BinOpASTNode.BinOp.CONCAT,
                (ExpressionASTNode) visitStringExpression(ctx.left),
                (ExpressionASTNode) visitStringExpression(ctx.right)
        );
    }

    @Override
    public ASTNode visitStrReturningFunc(SBGrammarParser.StrReturningFuncContext ctx) {
        return visitCallExternalFunction(ctx.callExternalFunction());
    }

    @Override
    public @NotNull ASTNode visitStringLiteral(SBGrammarParser.@NotNull StringLiteralContext ctx) {
        String str = ctx.getText();
        return new StringLiteralASTNode(str.substring(1, str.length() - 1));
    }

    @Override
    public @NotNull ASTNode visitSParens(SBGrammarParser.@NotNull SParensContext ctx) {
        return visitStringExpression(ctx.expr);
    }

    @Override
    public @NotNull ASTNode visitStringIdent(SBGrammarParser.@NotNull StringIdentContext ctx) {
        return new IdentifierASTNode(ctx.Ident().getText());
    }

    @Override
    public @NotNull ASTNode visitNumberIdent(SBGrammarParser.@NotNull NumberIdentContext ctx) {
        return new IdentifierASTNode(ctx.Ident().getText());
    }

    @Override
    public @NotNull ASTNode visitNParens(SBGrammarParser.@NotNull NParensContext ctx) {
        return visitArithExpression(ctx.expr);
    }

    @Override
    public @NotNull ASTNode visitDivMul(SBGrammarParser.@NotNull DivMulContext ctx) {
        return new BinOpASTNode(
                BinOpASTNode.BinOp.parse(ctx.op.getText()),
                (ExpressionASTNode) visitArithExpression(ctx.left),
                (ExpressionASTNode) visitArithExpression(ctx.right)
        );
    }

    @Override
    public @NotNull ASTNode visitPlusMin(@NotNull SBGrammarParser.PlusMinContext ctx) {
        return new BinOpASTNode(
                BinOpASTNode.BinOp.parse(ctx.op.getText()),
                (ExpressionASTNode) visitArithExpression(ctx.left),
                (ExpressionASTNode) visitArithExpression(ctx.right)
        );
    }

    @Override
    public ASTNode visitNumberReturningFunc(SBGrammarParser.NumberReturningFuncContext ctx) {
        return visitCallExternalFunction(ctx.callExternalFunction());
    }

    @Override
    public @NotNull ASTNode visitNumberLiteral(SBGrammarParser.@NotNull NumberLiteralContext ctx) {
        return NumberLiteralASTNode.parse(ctx.Number().getText());
    }

    @Override
    public @NotNull ASTNode visitCallRoutine(SBGrammarParser.@NotNull CallRoutineContext ctx) {
        String funcName = ctx.FunctionCall().getText();
        return new RoutineCallASTNode(funcName.replaceAll("\\([\\t ]*\\)", ""));
    }

    @Override
    public ASTNode visitCallExternalFunction(SBGrammarParser.CallExternalFunctionContext ctx) {
        String[] funcCall = ctx.name.getText().split("\\.");
        return new ExternalFunctionCallASTNode(
                funcCall[0],
                funcCall[1].substring(0, funcCall[1].length() - 1),
                ctx.args.stream()
                        .map(x -> (ExpressionASTNode) visitExpression(x))
                        .toList()
        );
    }

    public @NotNull ASTNode visitStatement(SBGrammarParser.@NotNull StatementContext ctx) {
        return ctx.getChild(0).accept(this);
    }

    @Override
    public @NotNull ASTNode visitSubroutineDecl(SBGrammarParser.@NotNull SubroutineDeclContext ctx) {
        return new RoutineDeclASTNode(ctx.name.getText(), childrenToAST(ctx.body));
    }

    @Override
    public @NotNull ASTNode visitExpression(SBGrammarParser.@NotNull ExpressionContext ctx) {
        return ctx.getChild(0).accept(this);
    }

    public @NotNull ASTNode visitBooleanExpression(SBGrammarParser.@NotNull BooleanExpressionContext ctx) {
        return ctx.accept(this);
    }

    public @NotNull ASTNode visitStringExpression(SBGrammarParser.@NotNull StringExpressionContext ctx) {
        return ctx.accept(this);
    }

    public @NotNull ASTNode visitArithExpression(SBGrammarParser.@NotNull ArithExpressionContext ctx) {
        return ctx.accept(this);
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
