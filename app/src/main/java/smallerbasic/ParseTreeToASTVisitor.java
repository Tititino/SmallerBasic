package smallerbasic;

import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import smallerbasic.AST.nodes.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

/**
 * This will make heavy use of class casting
 */
class ParseTreeToASTVisitor implements SBGrammarVisitor<ASTNode>  {

    private List<StatementASTNode> childrenToAST(List<SBGrammarParser.StatementContext> ctxs) {
        return ctxs.stream()
                .map(x -> (StatementASTNode) visitStatement(x))
                .toList();
    }

    @Override
    public ASTNode visitProgram(SBGrammarParser.ProgramContext ctx) {
        List<DeclOrStmtASTNode> body = IntStream.rangeClosed(0, ctx.getChildCount())
                .mapToObj(i -> {
                    try {
                        return (StatementASTNode) visitStatement(ctx.statement(i));
                    } catch (ClassCastException e) {
                        return (RoutineDeclASTNode) visitSubroutineDecl(ctx.subroutineDecl(i));
                    }
                }).toList();
        return new ProgramASTNode(body);
    }

    @Override
    public ASTNode visitAssignmentStmt(SBGrammarParser.AssignmentStmtContext ctx) {
        try {
            return new AssStmtASTNode(new IdentifierASTNode(ctx.Ident(0).getText())
                    , (ExpressionASTNode) visitExpression(ctx.expression()));
        } catch (ClassCastException e) {
            return new AssStmtASTNode(
                    new IdentifierASTNode(ctx.Ident(0).getText()),
                    new IdentifierASTNode(ctx.Ident(1).getText()));
        }
    }

    @Override
    public ASTNode visitLabel(SBGrammarParser.LabelContext ctx) {
        return new LabelDeclASTNode(ctx.Ident().getText());
    }

    @Override
    public ASTNode visitIfStmt(SBGrammarParser.IfStmtContext ctx) {
        ExpressionASTNode condition = (ExpressionASTNode) visitBooleanExpression(ctx.cond);
        List<StatementASTNode> trueBody = childrenToAST(ctx.bodyTrue);
        if (ctx.getChildCount() == 2)
            return new IfThenASTNode(condition, trueBody);
        else
            return new IfThenASTNode(condition, trueBody, childrenToAST(ctx.bodyFalse));
    }

    @Override
    public ASTNode visitForStmt(SBGrammarParser.ForStmtContext ctx) {
        IdentifierASTNode varName = new IdentifierASTNode(ctx.var.getText());
        ExpressionASTNode from = (ExpressionASTNode) visitArithExpression(ctx.from);
        ExpressionASTNode to   = (ExpressionASTNode) visitArithExpression(ctx.to);
        List<StatementASTNode> body = childrenToAST(ctx.body);
        if (ctx.getChildCount() == 4)
            return new ForLoopASTNode(varName, from, to, body);
        else
            return new ForLoopASTNode(varName, from, to,
                    (ExpressionASTNode) visitArithExpression(ctx.step),
                    body
            );
    }

    @Override
    public ASTNode visitWhileStmt(SBGrammarParser.WhileStmtContext ctx) {
        return new WhileLoopASTNode(
                (ExpressionASTNode) visitBooleanExpression(ctx.cond),
                childrenToAST(ctx.body)
        );
    }

    @Override
    public ASTNode visitGotoStmt(SBGrammarParser.GotoStmtContext ctx) {
        return new GotoStmtASTNode(ctx.Ident().getText());
    }

    @Override
    public ASTNode visitBParens(SBGrammarParser.BParensContext ctx) {
        return visitBooleanExpression(ctx.expr);
    }

    @Override
    public ASTNode visitStringComparison(SBGrammarParser.StringComparisonContext ctx) {
        return new BinOpASTNode(
                BinOpASTNode.BinOp.parse(ctx.relop.getText()),
                (ExpressionASTNode) visitStringExpression(ctx.left),
                (ExpressionASTNode) visitStringExpression(ctx.right)
        );
    }

    @Override
    public ASTNode visitBoolLiteral(SBGrammarParser.BoolLiteralContext ctx) {
        return BoolLiteralASTNode.parse(ctx.Bool().getText());
    }

    @Override
    public ASTNode visitNumberComparison(SBGrammarParser.NumberComparisonContext ctx) {
        return new BinOpASTNode(
                BinOpASTNode.BinOp.parse(ctx.relop.getText()),
                (ExpressionASTNode) visitArithExpression(ctx.left),
                (ExpressionASTNode) visitArithExpression(ctx.right)
        );
    }

    @Override
    public ASTNode visitBoolOp(SBGrammarParser.BoolOpContext ctx) {
        return new BinOpASTNode(
                BinOpASTNode.BinOp.parse(ctx.binop.getText()),
                (ExpressionASTNode) visitBooleanExpression(ctx.left),
                (ExpressionASTNode) visitBooleanExpression(ctx.right)
        );
    }

    @Override
    public ASTNode visitBoolIdent(SBGrammarParser.BoolIdentContext ctx) {
        return new IdentifierASTNode(ctx.Ident().getText());
    }

    @Override
    public ASTNode visitStringConcat(SBGrammarParser.StringConcatContext ctx) {
        return new BinOpASTNode(
                BinOpASTNode.BinOp.CONCAT,
                (ExpressionASTNode) visitStringExpression(ctx.left),
                (ExpressionASTNode) visitStringExpression(ctx.right)
        );
    }

    @Override
    public ASTNode visitStringLiteral(SBGrammarParser.StringLiteralContext ctx) {
        String str = ctx.getText();
        return new StringLiteralASTNode(str.substring(1, str.length() - 1));
    }

    @Override
    public ASTNode visitSParens(SBGrammarParser.SParensContext ctx) {
        return visitStringExpression(ctx.expr);
    }

    @Override
    public ASTNode visitStringIdent(SBGrammarParser.StringIdentContext ctx) {
        return new IdentifierASTNode(ctx.Ident().getText());
    }

    @Override
    public ASTNode visitNumberIdent(SBGrammarParser.NumberIdentContext ctx) {
        return new IdentifierASTNode(ctx.Ident().getText());
    }

    @Override
    public ASTNode visitNParens(SBGrammarParser.NParensContext ctx) {
        return visitArithExpression(ctx.expr);
    }

    @Override
    public ASTNode visitDivMul(SBGrammarParser.DivMulContext ctx) {
        return new BinOpASTNode(
                BinOpASTNode.BinOp.parse(ctx.op.getText()),
                (ExpressionASTNode) visitArithExpression(ctx.left),
                (ExpressionASTNode) visitArithExpression(ctx.right)
        );
    }

    @Override
    public ASTNode visitPlusMin(SBGrammarParser.PlusMinContext ctx) {
        return new BinOpASTNode(
                BinOpASTNode.BinOp.parse(ctx.op.getText()),
                (ExpressionASTNode) visitArithExpression(ctx.left),
                (ExpressionASTNode) visitArithExpression(ctx.right)
        );
    }

    @Override
    public ASTNode visitNumberLiteral(SBGrammarParser.NumberLiteralContext ctx) {
        return NumberLiteralASTNode.parse(ctx.Number().getText());
    }

    @Override
    public ASTNode visitCallRoutine(SBGrammarParser.CallRoutineContext ctx) {
        return new RoutineCallASTNode("", ctx.FunctionCall().getText(), Collections.emptyList());
    }

    // This is hideous
    public ASTNode visitStatement(SBGrammarParser.StatementContext ctx) {
        try {
            return visitAssignmentStmt(ctx.assignmentStmt());
        } catch (ClassCastException e) {
            try {
                return visitForStmt(ctx.forStmt());
            } catch (ClassCastException e1) {
                try {
                    return visitIfStmt(ctx.ifStmt());
                } catch (ClassCastException e2) {
                    try {
                        return visitWhileStmt(ctx.whileStmt());
                    } catch (ClassCastException e3) {
                        try {
                            return visitGotoStmt(ctx.gotoStmt());
                        } catch (ClassCastException e4) {
                            try {
                                return visitCallRoutine(ctx.callRoutine());
                            } catch (ClassCastException e5) {
                                return visitLabel(ctx.label());
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public ASTNode visitSubroutineDecl(SBGrammarParser.SubroutineDeclContext ctx) {
        return new RoutineDeclASTNode(ctx.name.getText(), childrenToAST(ctx.body));
    }

    @Override
    public ASTNode visitExpression(SBGrammarParser.ExpressionContext ctx) {
        try {
            return visitArithExpression(ctx.arithExpression());
        } catch (ClassCastException e) {
            try {
                return visitStringExpression(ctx.stringExpression());
            } catch (ClassCastException e1) {
                return visitBooleanExpression(ctx.booleanExpression());
            }
        }
    }

    public ASTNode visitBooleanExpression(SBGrammarParser.BooleanExpressionContext ctx) {
        if (ctx instanceof SBGrammarParser.NumberComparisonContext numComp)
            return visitNumberComparison(numComp);
        else if (ctx instanceof SBGrammarParser.StringComparisonContext strComp)
            return visitStringComparison(strComp);
        else if (ctx instanceof SBGrammarParser.BoolOpContext boolOp)
            return visitBoolOp(boolOp);
        else if (ctx instanceof SBGrammarParser.BParensContext bParens)
            return visitBParens(bParens);
        else if (ctx instanceof SBGrammarParser.BoolLiteralContext bLit)
            return visitBoolLiteral(bLit);
        else
            return visitBoolIdent((SBGrammarParser.BoolIdentContext) ctx);
    }

    public ASTNode visitStringExpression(SBGrammarParser.StringExpressionContext ctx) {
        if (ctx instanceof SBGrammarParser.StringConcatContext strConcat)
            return visitStringConcat(strConcat);
        else if (ctx instanceof SBGrammarParser.SParensContext sParens)
            return visitSParens(sParens);
        else if (ctx instanceof SBGrammarParser.StringLiteralContext sLit)
            return visitStringLiteral(sLit);
        else
            return visitStringIdent((SBGrammarParser.StringIdentContext) ctx);
    }

    public ASTNode visitArithExpression(SBGrammarParser.ArithExpressionContext ctx) {
        if (ctx instanceof SBGrammarParser.DivMulContext divMul)
            return visitDivMul(divMul);
        else if (ctx instanceof SBGrammarParser.PlusMinContext plusMin)
            return visitPlusMin(plusMin);
        else if (ctx instanceof SBGrammarParser.NParensContext nParens)
            return visitNParens(nParens);
        else if (ctx instanceof SBGrammarParser.NumberLiteralContext nLit)
            return visitNumberLiteral(nLit);
        else
            return visitNumberIdent((SBGrammarParser.NumberIdentContext) ctx);
    }


    @Override
    public ASTNode visit(ParseTree tree) {
        return null;
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
