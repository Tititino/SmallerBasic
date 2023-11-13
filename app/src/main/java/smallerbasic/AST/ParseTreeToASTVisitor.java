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
 * The behaviour is undefined if the parse is generated from a wrong input.
 */
public class ParseTreeToASTVisitor implements SBGrammarVisitor<ASTNode> {

    /**
     * The current scope being visited: {@code Scope.ofRoutine(<routine name>)} if inside a routine declaration,
     * {@code Scope.TOPLEVEL} otherwise.
     */
    private @NotNull Scope currentScope = Scope.TOPLEVEL;

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
        return new LabelNameASTNode(ctx.Ident().getText(), currentScope);
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
        currentScope = Scope.ofRoutine(name);
        RoutineDeclASTNode node = new RoutineDeclASTNode(name, childrenToAST(ctx.body));
        currentScope = Scope.TOPLEVEL;
        return node;
    }

    @Override
    public ASTNode visitPlusMinExpr(SBGrammarParser.PlusMinExprContext ctx) {
        ExpressionASTNode left = (ExpressionASTNode) visit(ctx.left);
        ExpressionASTNode right = (ExpressionASTNode) visit(ctx.right);
        return new BinOpASTNode(BinOpASTNode.BinOp.parse(ctx.op.getText()), left, right);
    }

    @Override
    public ASTNode visitMulDivExpr(SBGrammarParser.MulDivExprContext ctx) {
        ExpressionASTNode left = (ExpressionASTNode) visit(ctx.left);
        ExpressionASTNode right = (ExpressionASTNode) visit(ctx.right);
        return new BinOpASTNode(BinOpASTNode.BinOp.parse(ctx.op.getText()), left, right);
    }

    @Override
    public ASTNode visitBoolopExpr(SBGrammarParser.BoolopExprContext ctx) {
        ExpressionASTNode left = (ExpressionASTNode) visit(ctx.left);
        ExpressionASTNode right = (ExpressionASTNode) visit(ctx.right);
        return new BinOpASTNode(BinOpASTNode.BinOp.parse(ctx.op.getText()), left, right);
    }

    @Override
    public ASTNode visitRelopExpr(SBGrammarParser.RelopExprContext ctx) {
        ExpressionASTNode left = (ExpressionASTNode) visit(ctx.left);
        ExpressionASTNode right = (ExpressionASTNode) visit(ctx.right);
        return new BinOpASTNode(BinOpASTNode.BinOp.parse(ctx.op.getText()), left, right);
    }

    @Override
    public ASTNode visitAtomExpr(SBGrammarParser.AtomExprContext ctx) {
        return visit(ctx.atom());
    }

    @Override
    public ASTNode visitUnaryMinusExpr(SBGrammarParser.UnaryMinusExprContext ctx) {
        ExpressionASTNode body = (ExpressionASTNode) visit(ctx.val);
        return new UnaryMinusASTNode(body);
    }

    @Override
    public ASTNode visitStringLit(SBGrammarParser.StringLitContext ctx) {
        String str = ctx.getText();
        return new StringLiteralASTNode(str.substring(1, str.length() - 1));
    }

    @Override
    public ASTNode visitNumberLit(SBGrammarParser.NumberLitContext ctx) {
        return NumberLiteralASTNode.parse(ctx.getText());
    }

    @Override
    public ASTNode visitBoolLit(SBGrammarParser.BoolLitContext ctx) {
        return BoolLiteralASTNode.parse(ctx.Bool().getText());
    }

    @Override
    public ASTNode visitVarExpr(SBGrammarParser.VarExprContext ctx) {
        return visit(ctx.variable());
    }

    @Override
    public ASTNode visitExtern(SBGrammarParser.ExternContext ctx) {
        return visit(ctx.callExternalFunction());
    }

    @Override
    public ASTNode visitParens(SBGrammarParser.ParensContext ctx) {
        return visit(ctx.body);
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
