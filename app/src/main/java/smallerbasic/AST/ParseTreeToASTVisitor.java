package smallerbasic.AST;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.jetbrains.annotations.NotNull;
import smallerbasic.AST.nodes.*;
import smallerbasic.SBGrammarParser;
import smallerbasic.SBGrammarVisitor;

import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

public class ParseTreeToASTVisitor implements SBGrammarVisitor<ASTNode> {

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
                .filter(i -> !(ctx.getChild(i) instanceof TerminalNode))
                .mapToObj(i ->
                    (DeclOrStmtASTNode) visit(ctx.getChild(i))
                ).toList();
        return setTokens(new ProgramASTNode(body), ctx);
    }

    @Override
    public @NotNull ASTNode visitAssignmentStmt(SBGrammarParser.@NotNull AssignmentStmtContext ctx) {
        VariableASTNode left = (VariableASTNode) ctx.var.accept(this);
        ExpressionASTNode right = (ExpressionASTNode) ctx.expression().accept(this);
        return setTokens(new AssStmtASTNode(left, right), ctx);
    }


    @Override
    public @NotNull ASTNode visitVar(SBGrammarParser.@NotNull VarContext ctx) {
        return ctx.accept(this);
    }

    @Override
    public @NotNull ASTNode visitArray(SBGrammarParser.@NotNull ArrayContext ctx) {
        IdentifierASTNode name = setToken(
                new IdentifierASTNode(
                        ctx.name.getText().replaceAll("\\[", "")),
                ctx.name
        );
        ExpressionASTNode expr = (ExpressionASTNode) ctx.arithExpression().accept(this);
        return setTokens(new ArrayASTNode(name, expr), ctx);
    }

    @Override
    public @NotNull ASTNode visitLabel(SBGrammarParser.@NotNull LabelContext ctx) {
        LabelNameASTNode label = setToken(new LabelNameASTNode(ctx.Ident().getText()), ctx.Ident().getSymbol());
        return setTokens(new LabelDeclASTNode(label), ctx);
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
        LabelNameASTNode label = setToken(new LabelNameASTNode(ctx.Ident().getText()), ctx.Ident().getSymbol());
        return setTokens(new GotoStmtASTNode(label), ctx);
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
    public @NotNull ASTNode visitBoolVar(SBGrammarParser.@NotNull BoolVarContext ctx) {
        return ctx.variable().accept(this);
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
    public @NotNull ASTNode visitStrReturningFunc(SBGrammarParser.@NotNull StrReturningFuncContext ctx) {
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
    public @NotNull ASTNode visitStrVar(SBGrammarParser.@NotNull StrVarContext ctx) {
        return ctx.variable().accept(this);
    }


    @Override
    public @NotNull ASTNode visitNParens(SBGrammarParser.@NotNull NParensContext ctx) {
        return visit(ctx.expr);
    }

    @Override
    public @NotNull ASTNode visitMinusVar(SBGrammarParser.@NotNull MinusVarContext ctx) {
        IdentifierASTNode var = setToken(new IdentifierASTNode(ctx.var.getText()), ctx.var);
        return setTokens(new UnaryMinusASTNode(var), ctx);
    }

    @Override
    public @NotNull ASTNode visitNumberVar(SBGrammarParser.@NotNull NumberVarContext ctx) {
        return ctx.variable().accept(this);
    }

    @Override
    public @NotNull ASTNode visitUnaryMinus(SBGrammarParser.@NotNull UnaryMinusContext ctx) {
        ExpressionASTNode expr = (ExpressionASTNode) visit(ctx.expr);
        return setTokens(new UnaryMinusASTNode(expr), ctx);
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
    public @NotNull ASTNode visitNumberReturningFunc(SBGrammarParser.@NotNull NumberReturningFuncContext ctx) {
        return visit(ctx.callExternalFunction());
    }

    @Override
    public @NotNull ASTNode visitNumberLiteral(SBGrammarParser.@NotNull NumberLiteralContext ctx) {
        return setTokens(NumberLiteralASTNode.parse(ctx.Number().getText()), ctx);
    }

    @Override
    public @NotNull ASTNode visitCallRoutine(SBGrammarParser.@NotNull CallRoutineContext ctx) {
        RoutineNameASTNode name = setToken(
                new RoutineNameASTNode(
                        ctx.getText().replaceAll("\\([\\t ]*\\)", "")
                ), ctx.FunctionCall().getSymbol()
        );
        return setTokens(new RoutineCallASTNode(name), ctx);
    }

    @Override
    public @NotNull ASTNode visitCallExternalFunction(SBGrammarParser.@NotNull CallExternalFunctionContext ctx) {
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
        RoutineNameASTNode name = setToken(
                new RoutineNameASTNode(ctx.name.getText()),
                ctx.name
        );
        return setTokens(new RoutineDeclASTNode(name, childrenToAST(ctx.body)), ctx);
    }

    @Override
    public @NotNull ASTNode visitExpression(SBGrammarParser.@NotNull ExpressionContext ctx) {
        return visit(ctx.getChild(0));
    }

    public @NotNull ASTNode visitBooleanExpression(SBGrammarParser.@NotNull BooleanExpressionContext ctx) {
        return visit(ctx);
    }

    @Override
    public @NotNull ASTNode visit(@NotNull ParseTree tree) {
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
