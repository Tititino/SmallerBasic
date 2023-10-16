package smallerbasic;

import org.antlr.v4.runtime.RuleContext;
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
        return null;
    }

    @Override
    public ASTNode visitBooleanExpression(SBGrammarParser.BooleanExpressionContext ctx) {
        return null;
    }

    @Override
    public ASTNode visitStringExpression(SBGrammarParser.StringExpressionContext ctx) {
        return null;
    }

    @Override
    public ASTNode visitArithExpression(SBGrammarParser.ArithExpressionContext ctx) {
        return null;
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
