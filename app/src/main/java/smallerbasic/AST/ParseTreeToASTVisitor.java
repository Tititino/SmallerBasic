package smallerbasic.AST;

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

    protected @NotNull List<StatementASTNode> childrenToAST(@NotNull List<SBGrammarParser.StatementContext> ctxs) {
        return ctxs.stream()
                .map(x -> (StatementASTNode) visit(x))
                .toList();
    }

    @Override
    public @NotNull ProgramASTNode visitProgram(SBGrammarParser.@NotNull ProgramContext ctx) {
        List<DeclOrStmtASTNode> body = IntStream.range(0, ctx.getChildCount() - 1)
                .mapToObj(i ->
                        (DeclOrStmtASTNode) visit(ctx.getChild(i))
                ).toList();
        return new ProgramASTNode(body);
    }

    @Override
    public @NotNull AssStmtASTNode visitAssignmentStmt(SBGrammarParser.@NotNull AssignmentStmtContext ctx) {
        VariableASTNode left = (VariableASTNode) visit(ctx.variable());
        ExpressionASTNode right = (ExpressionASTNode) visit(ctx.expression());
        return new AssStmtASTNode(left, right);
    }


    @Override
    public @NotNull IdentifierASTNode visitVar(SBGrammarParser.@NotNull VarContext ctx) {
        return new IdentifierASTNode(ctx.name.getText());
    }

    @Override
    public @NotNull ArrayASTNode visitArray(SBGrammarParser.@NotNull ArrayContext ctx) {
        IdentifierASTNode name = (IdentifierASTNode) visit(ctx.name);
        List<ExpressionASTNode> expr = ctx.expr
                .stream()
                .map(x -> (ExpressionASTNode) visit(x))
                .toList();
        return new ArrayASTNode(name, expr);
    }

    @Override
    public @NotNull LabelDeclASTNode visitLabel(SBGrammarParser.@NotNull LabelContext ctx) {
        LabelNameASTNode label = (LabelNameASTNode) visit(ctx.labelName());
        return new LabelDeclASTNode(label);
    }

    @Override
    public @NotNull IdentifierASTNode visitVarName(SBGrammarParser.@NotNull VarNameContext ctx) {
        return new IdentifierASTNode(ctx.Ident().getText());
    }

    @Override
    public @NotNull LabelNameASTNode visitLabelName(SBGrammarParser.@NotNull LabelNameContext ctx) {
        return new LabelNameASTNode(ctx.Ident().getText());
    }

    @Override
    public @NotNull RoutineNameASTNode visitFunctionName(SBGrammarParser.@NotNull FunctionNameContext ctx) {
        return new RoutineNameASTNode(ctx.Ident().getText());
    }

    @Override
    public @NotNull IfThenASTNode visitIfStmt(SBGrammarParser.@NotNull IfStmtContext ctx) {
        ExpressionASTNode condition = (ExpressionASTNode) visit(ctx.cond);
        List<StatementASTNode> trueBody = childrenToAST(ctx.bodyTrue);
        if (ctx.bodyFalse.isEmpty())
            return new IfThenASTNode(condition, trueBody);
        else
            return new IfThenASTNode(condition, trueBody, childrenToAST(ctx.bodyFalse));
    }

    @Override
    public @NotNull ForLoopASTNode visitForStmt(SBGrammarParser.@NotNull ForStmtContext ctx) {
        VariableASTNode varName = (VariableASTNode) visit(ctx.var);
        ExpressionASTNode from = (ExpressionASTNode) visit(ctx.from);
        ExpressionASTNode to = (ExpressionASTNode) visit(ctx.to);
        List<StatementASTNode> body = childrenToAST(ctx.body);
        if (Objects.isNull(ctx.step))
            return new ForLoopASTNode(varName, from, to, body);
        else
            return new ForLoopASTNode(varName, from, to,
                    (ExpressionASTNode) visit(ctx.step),
                    body
            );
    }

    @Override
    public @NotNull WhileLoopASTNode visitWhileStmt(SBGrammarParser.@NotNull WhileStmtContext ctx) {
        return new WhileLoopASTNode(
                (ExpressionASTNode) visit(ctx.cond),
                childrenToAST(ctx.body)
        );
    }

    @Override
    public @NotNull GotoStmtASTNode visitGotoStmt(SBGrammarParser.@NotNull GotoStmtContext ctx) {
        LabelNameASTNode label = (LabelNameASTNode) visit(ctx.labelName());
        return new GotoStmtASTNode(label);
    }

    @Override
    public @NotNull ASTNode visitBParens(SBGrammarParser.@NotNull BParensContext ctx) {
        return visit(ctx.expr);
    }

    @Override
    public @NotNull BinOpASTNode visitStringComparison(SBGrammarParser.@NotNull StringComparisonContext ctx) {
        return new BinOpASTNode(
                BinOpASTNode.BinOp.parse(ctx.relop.getText()),
                (ExpressionASTNode) visit(ctx.left),
                (ExpressionASTNode) visit(ctx.right)
        );
    }

    @Override
    public @NotNull BoolLiteralASTNode visitBoolLiteral(SBGrammarParser.@NotNull BoolLiteralContext ctx) {
        return BoolLiteralASTNode.parse(ctx.Bool().getText());
    }

    @Override
    public ASTNode visitBoolReturningFunc(SBGrammarParser.BoolReturningFuncContext ctx) {
        return visitCallExternalFunction(ctx.callExternalFunction());
    }

    @Override
    public @NotNull BinOpASTNode visitNumberComparison(SBGrammarParser.@NotNull NumberComparisonContext ctx) {
        return new BinOpASTNode(
                BinOpASTNode.BinOp.parse(ctx.relop.getText()),
                (ExpressionASTNode) visit(ctx.left),
                (ExpressionASTNode) visit(ctx.right)
        );
    }

    @Override
    public @NotNull BinOpASTNode visitBoolOp(SBGrammarParser.@NotNull BoolOpContext ctx) {
        return new BinOpASTNode(
                BinOpASTNode.BinOp.parse(ctx.binop.getText()),
                (ExpressionASTNode) visit(ctx.left),
                (ExpressionASTNode) visit(ctx.right)
        );
    }

    @Override
    public @NotNull ASTNode visitBoolVar(SBGrammarParser.@NotNull BoolVarContext ctx) {
        return visit(ctx.variable());
    }

    @Override
    public @NotNull BinOpASTNode visitStringConcat(SBGrammarParser.@NotNull StringConcatContext ctx) {
        return new BinOpASTNode(
                BinOpASTNode.BinOp.CONCAT,
                (ExpressionASTNode) visit(ctx.left),
                (ExpressionASTNode) visit(ctx.right)
        );
    }

    @Override
    public @NotNull ASTNode visitStrReturningFunc(SBGrammarParser.@NotNull StrReturningFuncContext ctx) {
        return visit(ctx.callExternalFunction());
    }

    @Override
    public @NotNull StringLiteralASTNode visitStringLiteral(SBGrammarParser.@NotNull StringLiteralContext ctx) {
        String str = ctx.getText();
        return new StringLiteralASTNode(str.substring(1, str.length() - 1));
    }

    @Override
    public @NotNull ASTNode visitSParens(SBGrammarParser.@NotNull SParensContext ctx) {
        return visit(ctx.expr);
    }

    @Override
    public @NotNull ASTNode visitStrVar(SBGrammarParser.@NotNull StrVarContext ctx) {
        return visit(ctx.variable());
    }

    @Override
    public @NotNull UnaryMinusASTNode visitUnaryMinus(SBGrammarParser.@NotNull UnaryMinusContext ctx) {
        ExpressionASTNode expr = (ExpressionASTNode) visit(ctx.arithAtom());
        return new UnaryMinusASTNode(expr);
    }

    @Override
    public @NotNull BinOpASTNode visitDivMul(SBGrammarParser.@NotNull DivMulContext ctx) {
        return new BinOpASTNode(
                BinOpASTNode.BinOp.parse(ctx.op.getText()),
                (ExpressionASTNode) visit(ctx.left),
                (ExpressionASTNode) visit(ctx.right)
        );
    }

    @Override
    public @NotNull BinOpASTNode visitPlusMin(@NotNull SBGrammarParser.PlusMinContext ctx) {
        return new BinOpASTNode(
                BinOpASTNode.BinOp.parse(ctx.op.getText()),
                (ExpressionASTNode) visit(ctx.left),
                (ExpressionASTNode) visit(ctx.right)
        );
    }

    @Override
    public ASTNode visitAtom(SBGrammarParser.AtomContext ctx) {
        return visit(ctx.arithAtom());
    }

    @Override
    public ASTNode visitVariableAtom(SBGrammarParser.VariableAtomContext ctx) {
        return visit(ctx.variable());
    }

    @Override
    public ASTNode visitExternalFuncAtom(SBGrammarParser.ExternalFuncAtomContext ctx) {
        return visit(ctx.callExternalFunction());
    }

    @Override
    public ASTNode visitParensAtom(SBGrammarParser.ParensAtomContext ctx) {
        return visit(ctx.arithExpression());
    }

    @Override
    public ASTNode visitLiteralAtom(SBGrammarParser.LiteralAtomContext ctx) {
        return NumberLiteralASTNode.parse(ctx.getText());
    }

    @Override
    public @NotNull RoutineCallASTNode visitCallRoutine(SBGrammarParser.@NotNull CallRoutineContext ctx) {
        RoutineNameASTNode name = (RoutineNameASTNode) visit(ctx.functionName());
        return new RoutineCallASTNode(name);
    }

    @Override
    public @NotNull ExternalFunctionCallASTNode visitCallExternalFunction(SBGrammarParser.@NotNull CallExternalFunctionContext ctx) {
        String[] funcCall = ctx.name.getText().split("\\.");
        return new ExternalFunctionCallASTNode(
                funcCall[0],
                funcCall[1],
                ctx.args.stream()
                        .map(x -> (ExpressionASTNode) visit(x))
                        .toList()
        );
    }

    public @NotNull ASTNode visitStatement(SBGrammarParser.@NotNull StatementContext ctx) {
        return visit(ctx.getChild(0));
    }

    @Override
    public @NotNull RoutineDeclASTNode visitSubroutineDecl(SBGrammarParser.@NotNull SubroutineDeclContext ctx) {
        RoutineNameASTNode name = (RoutineNameASTNode) visit(ctx.functionName());
        return new RoutineDeclASTNode(name, childrenToAST(ctx.body));
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
