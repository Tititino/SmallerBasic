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

/**
 * A class to convert a {@link ParseTree} made by {@link SBGrammarParser} to an AST.
 * This class accepts only well-formed parse trees.
 */
public class ParseTreeToASTVisitor implements SBGrammarVisitor<ASTNode> {

    private @NotNull List<StatementASTNode> childrenToAST(@NotNull List<SBGrammarParser.StatementContext> ctxs) {
        return ctxs.stream()
                .map(x -> (StatementASTNode) visit(x))
                .toList();
    }

    @Override
    public ProgramASTNode visitProgram(SBGrammarParser.ProgramContext ctx) {
        List<DeclOrStmtASTNode> body = IntStream.range(0, ctx.getChildCount() - 1)
                .mapToObj(i ->
                        (DeclOrStmtASTNode) visit(ctx.getChild(i))
                ).toList();
        return new ProgramASTNode(body);
    }

    @Override
    public AssStmtASTNode visitAssignmentStmt(SBGrammarParser.AssignmentStmtContext ctx) {
        VariableASTNode left = (VariableASTNode) visit(ctx.variable());
        ExpressionASTNode right = (ExpressionASTNode) visit(ctx.expression());
        return new AssStmtASTNode(left, right);
    }


    @Override
    public IdentifierASTNode visitVar(SBGrammarParser.VarContext ctx) {
        return new IdentifierASTNode(ctx.name.getText());
    }

    @Override
    public ArrayASTNode visitArray(SBGrammarParser.ArrayContext ctx) {
        IdentifierASTNode name = (IdentifierASTNode) visit(ctx.name);
        List<ExpressionASTNode> expr = ctx.expr
                .stream()
                .map(x -> (ExpressionASTNode) visit(x))
                .toList();
        return new ArrayASTNode(name, expr);
    }

    @Override
    public LabelDeclASTNode visitLabel(SBGrammarParser.LabelContext ctx) {
        LabelNameASTNode label = (LabelNameASTNode) visit(ctx.labelName());
        return new LabelDeclASTNode(label);
    }

    @Override
    public IdentifierASTNode visitVarName(SBGrammarParser.VarNameContext ctx) {
        return new IdentifierASTNode(ctx.Ident().getText());
    }

    @Override
    public LabelNameASTNode visitLabelName(SBGrammarParser.LabelNameContext ctx) {
        return new LabelNameASTNode(ctx.Ident().getText());
    }

    @Override
    public RoutineNameASTNode visitFunctionName(SBGrammarParser.FunctionNameContext ctx) {
        return new RoutineNameASTNode(ctx.Ident().getText());
    }

    @Override
    public IfThenASTNode visitIfStmt(SBGrammarParser.IfStmtContext ctx) {
        ExpressionASTNode condition = (ExpressionASTNode) visit(ctx.cond);
        List<StatementASTNode> trueBody = childrenToAST(ctx.bodyTrue);
        if (ctx.bodyFalse.isEmpty())
            return new IfThenASTNode(condition, trueBody);
        else
            return new IfThenASTNode(condition, trueBody, childrenToAST(ctx.bodyFalse));
    }

    @Override
    public ForLoopASTNode visitForStmt(SBGrammarParser.ForStmtContext ctx) {
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
    public WhileLoopASTNode visitWhileStmt(SBGrammarParser.WhileStmtContext ctx) {
        return new WhileLoopASTNode(
                (ExpressionASTNode) visit(ctx.cond),
                childrenToAST(ctx.body)
        );
    }

    @Override
    public GotoStmtASTNode visitGotoStmt(SBGrammarParser.GotoStmtContext ctx) {
        LabelNameASTNode label = (LabelNameASTNode) visit(ctx.labelName());
        return new GotoStmtASTNode(label);
    }

    @Override
    public ASTNode visitBParens(SBGrammarParser.BParensContext ctx) {
        return visit(ctx.expr);
    }

    @Override
    public BinOpASTNode visitStringComparison(SBGrammarParser.StringComparisonContext ctx) {
        return new BinOpASTNode(
                BinOpASTNode.BinOp.parse(ctx.relop.getText()),
                (ExpressionASTNode) visit(ctx.left),
                (ExpressionASTNode) visit(ctx.right)
        );
    }

    @Override
    public BoolLiteralASTNode visitBoolLiteral(SBGrammarParser.BoolLiteralContext ctx) {
        return BoolLiteralASTNode.parse(ctx.Bool().getText());
    }

    @Override
    public ASTNode visitBoolReturningFunc(SBGrammarParser.BoolReturningFuncContext ctx) {
        return visitCallExternalFunction(ctx.callExternalFunction());
    }

    @Override
    public BinOpASTNode visitNumberComparison(SBGrammarParser.NumberComparisonContext ctx) {
        return new BinOpASTNode(
                BinOpASTNode.BinOp.parse(ctx.relop.getText()),
                (ExpressionASTNode) visit(ctx.left),
                (ExpressionASTNode) visit(ctx.right)
        );
    }

    @Override
    public BinOpASTNode visitBoolOp(SBGrammarParser.BoolOpContext ctx) {
        return new BinOpASTNode(
                BinOpASTNode.BinOp.parse(ctx.binop.getText()),
                (ExpressionASTNode) visit(ctx.left),
                (ExpressionASTNode) visit(ctx.right)
        );
    }

    @Override
    public ASTNode visitBoolVar(SBGrammarParser.BoolVarContext ctx) {
        return visit(ctx.variable());
    }

    @Override
    public BinOpASTNode visitStringConcat(SBGrammarParser.StringConcatContext ctx) {
        return new BinOpASTNode(
                BinOpASTNode.BinOp.CONCAT,
                (ExpressionASTNode) visit(ctx.left),
                (ExpressionASTNode) visit(ctx.right)
        );
    }

    @Override
    public ASTNode visitStrReturningFunc(SBGrammarParser.StrReturningFuncContext ctx) {
        return visit(ctx.callExternalFunction());
    }

    @Override
    public StringLiteralASTNode visitStringLiteral(SBGrammarParser.StringLiteralContext ctx) {
        String str = ctx.getText();
        return new StringLiteralASTNode(str.substring(1, str.length() - 1));
    }

    @Override
    public ASTNode visitSParens(SBGrammarParser.SParensContext ctx) {
        return visit(ctx.expr);
    }

    @Override
    public ASTNode visitStrVar(SBGrammarParser.StrVarContext ctx) {
        return visit(ctx.variable());
    }

    @Override
    public UnaryMinusASTNode visitUnaryMinus(SBGrammarParser.UnaryMinusContext ctx) {
        ExpressionASTNode expr = (ExpressionASTNode) visit(ctx.arithAtom());
        return new UnaryMinusASTNode(expr);
    }

    @Override
    public BinOpASTNode visitDivMul(SBGrammarParser.DivMulContext ctx) {
        return new BinOpASTNode(
                BinOpASTNode.BinOp.parse(ctx.op.getText()),
                (ExpressionASTNode) visit(ctx.left),
                (ExpressionASTNode) visit(ctx.right)
        );
    }

    @Override
    public BinOpASTNode visitPlusMin(SBGrammarParser.PlusMinContext ctx) {
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
    public RoutineCallASTNode visitCallRoutine(SBGrammarParser.CallRoutineContext ctx) {
        RoutineNameASTNode name = (RoutineNameASTNode) visit(ctx.functionName());
        return new RoutineCallASTNode(name);
    }

    @Override
    public ExternalFunctionCallASTNode visitCallExternalFunction(SBGrammarParser.CallExternalFunctionContext ctx) {
        String[] funcCall = ctx.name.getText().split("\\.");
        return new ExternalFunctionCallASTNode(
                funcCall[0],
                funcCall[1],
                ctx.args.stream()
                        .map(x -> (ExpressionASTNode) visit(x))
                        .toList()
        );
    }

    public ASTNode visitStatement(SBGrammarParser.StatementContext ctx) {
        return visit(ctx.getChild(0));
    }

    @Override
    public RoutineDeclASTNode visitSubroutineDecl(SBGrammarParser.SubroutineDeclContext ctx) {
        RoutineNameASTNode name = (RoutineNameASTNode) visit(ctx.functionName());
        return new RoutineDeclASTNode(name, childrenToAST(ctx.body));
    }

    @Override
    public ASTNode visitExpression(SBGrammarParser.ExpressionContext ctx) {
        return visit(ctx.getChild(0));
    }

    public ASTNode visitBooleanExpression(SBGrammarParser.BooleanExpressionContext ctx) {
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
