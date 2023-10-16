package smallerbasic;

import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import smallerbasic.AST.ASTNode;

public class ParseTreeToASTVisitor implements SBGrammarVisitor<ASTNode>  {
    @Override
    public ASTNode visitProgram(SBGrammarParser.ProgramContext ctx) {
        // List<DeclOrStmtASTNode> body = visitSubroutineDecl(ctx.subroutineDecl(i))
        return null;
    }

    @Override
    public ASTNode visitAssignmentStmt(SBGrammarParser.AssignmentStmtContext ctx) {
        return null;
    }

    @Override
    public ASTNode visitLabel(SBGrammarParser.LabelContext ctx) {
        return null;
    }

    @Override
    public ASTNode visitStatement(SBGrammarParser.StatementContext ctx) {
        return null;
    }

    @Override
    public ASTNode visitCallRoutine(SBGrammarParser.CallRoutineContext ctx) {
        return null;
    }

    @Override
    public ASTNode visitSubroutineDecl(SBGrammarParser.SubroutineDeclContext ctx) {
        return null;
    }

    @Override
    public ASTNode visitExpression(SBGrammarParser.ExpressionContext ctx) {
        return null;
    }

    @Override
    public ASTNode visitIfStmt(SBGrammarParser.IfStmtContext ctx) {
        return null;
    }

    @Override
    public ASTNode visitForStmt(SBGrammarParser.ForStmtContext ctx) {
        return null;
    }

    @Override
    public ASTNode visitWhileStmt(SBGrammarParser.WhileStmtContext ctx) {
        return null;
    }

    @Override
    public ASTNode visitGotoStmt(SBGrammarParser.GotoStmtContext ctx) {
        return null;
    }

    @Override
    public ASTNode visitBParens(SBGrammarParser.BParensContext ctx) {
        return null;
    }

    @Override
    public ASTNode visitStringComparison(SBGrammarParser.StringComparisonContext ctx) {
        return null;
    }

    @Override
    public ASTNode visitBoolLiteral(SBGrammarParser.BoolLiteralContext ctx) {
        return null;
    }

    @Override
    public ASTNode visitNumberComparison(SBGrammarParser.NumberComparisonContext ctx) {
        return null;
    }

    @Override
    public ASTNode visitBIdentifier(SBGrammarParser.BIdentifierContext ctx) {
        return null;
    }

    @Override
    public ASTNode visitBoolean(SBGrammarParser.BooleanContext ctx) {
        return null;
    }

    @Override
    public ASTNode visitStringExpression(SBGrammarParser.StringExpressionContext ctx) {
        return null;
    }

    @Override
    public ASTNode visitPlusMinus(SBGrammarParser.PlusMinusContext ctx) {
        return null;
    }

    @Override
    public ASTNode visitAIdentifier(SBGrammarParser.AIdentifierContext ctx) {
        return null;
    }

    @Override
    public ASTNode visitMulDiv(SBGrammarParser.MulDivContext ctx) {
        return null;
    }

    @Override
    public ASTNode visitNumberLiteral(SBGrammarParser.NumberLiteralContext ctx) {
        return null;
    }

    @Override
    public ASTNode visitAParens(SBGrammarParser.AParensContext ctx) {
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
