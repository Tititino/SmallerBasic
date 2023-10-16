package smallerbasic;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.Test;
import smallerbasic.AST.nodes.*;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ASTConversionTest {
    @Test
    public void labelTest() {
        SBGrammarLexer lexer = new SBGrammarLexer(CharStreams.fromString("a = 10"));
        SBGrammarParser parser = new SBGrammarParser(new CommonTokenStream(lexer));
        SBGrammarParser.AssignmentStmtContext tree = parser.assignmentStmt();

        ASTNode expected = new AssStmtASTNode(new IdentifierASTNode("a"), NumberLiteralASTNode.parse("10"));
        ParseTreeToASTVisitor convert = new ParseTreeToASTVisitor();

        assertThat(convert.visitAssignmentStmt(tree)).isEqualTo(expected);
    }

    @Test
    public void stringComparisonTest() {
        SBGrammarLexer lexer = new SBGrammarLexer(CharStreams.fromString("(X + \"ciao\") = \"mondo\""));
        SBGrammarParser parser = new SBGrammarParser(new CommonTokenStream(lexer));
        SBGrammarParser.BooleanExpressionContext tree = parser.booleanExpression();

        ASTNode expected = new BinOpASTNode(
                BinOpASTNode.BinOp.EQ,
                new BinOpASTNode(
                        BinOpASTNode.BinOp.CONCAT,
                        new IdentifierASTNode("X"),
                        new StringLiteralASTNode("ciao")
                ),
                new StringLiteralASTNode("mondo")
        );
        ParseTreeToASTVisitor convert = new ParseTreeToASTVisitor();

        assertThat(convert.visitBooleanExpression(tree)).isEqualTo(expected);
    }

    @Test
    public void forTest() {
        SBGrammarLexer lexer = new SBGrammarLexer(CharStreams.fromString("For X = 3 To 10 Step 2\nX = 3\nEndFor"));
        SBGrammarParser parser = new SBGrammarParser(new CommonTokenStream(lexer));
        SBGrammarParser.ForStmtContext tree = parser.forStmt();

        ASTNode expected = new ForLoopASTNode(
                new IdentifierASTNode("X"),
                new NumberLiteralASTNode(3),
                new NumberLiteralASTNode(10),
                new NumberLiteralASTNode(2),
                List.of(new AssStmtASTNode(new IdentifierASTNode("X"), new NumberLiteralASTNode(3)))
        );
        ParseTreeToASTVisitor convert = new ParseTreeToASTVisitor();

        assertThat(convert.visitForStmt(tree)).isEqualTo(expected);
    }

    @Test
    public void routineDeclTest() {
        SBGrammarLexer lexer = new SBGrammarLexer(CharStreams.fromString("Sub test\nX = X + 3\nY = false\nEndSub\n"));
        SBGrammarParser parser = new SBGrammarParser(new CommonTokenStream(lexer));
        SBGrammarParser.SubroutineDeclContext tree = parser.subroutineDecl();

        ASTNode expected = new RoutineDeclASTNode(
                "test",
                List.of(
                        new AssStmtASTNode(
                                new IdentifierASTNode("X"),
                                new BinOpASTNode(
                                        BinOpASTNode.BinOp.PLUS,
                                        new IdentifierASTNode("X"),
                                        new NumberLiteralASTNode(3)
                                )
                        ),
                        new AssStmtASTNode(
                                new IdentifierASTNode("Y"),
                                BoolLiteralASTNode.parse("false")
                        )
                )
        );
        ParseTreeToASTVisitor convert = new ParseTreeToASTVisitor();

        assertThat(convert.visitSubroutineDecl(tree)).isEqualTo(expected);
    }

    @Test
    public void externFuncExpressionTest() {
        SBGrammarLexer lexer = new SBGrammarLexer(CharStreams.fromString("a = IO.readLine()"));
        SBGrammarParser parser = new SBGrammarParser(new CommonTokenStream(lexer));
        SBGrammarParser.AssignmentStmtContext tree = parser.assignmentStmt();

        ASTNode expected = new AssStmtASTNode(
                new IdentifierASTNode("a"),
                new ExternalFunctionCallASTNode("IO", "readLine", Collections.emptyList())
        );
        ParseTreeToASTVisitor convert = new ParseTreeToASTVisitor();

        assertThat(convert.visitAssignmentStmt(tree)).isEqualTo(expected);
    }

    @Test
    public void externFuncStatementTest() {
        SBGrammarLexer lexer = new SBGrammarLexer(CharStreams.fromString("If (true) Then\nIO.writeLine(X)\nEndIf"));
        SBGrammarParser parser = new SBGrammarParser(new CommonTokenStream(lexer));
        SBGrammarParser.IfStmtContext tree = parser.ifStmt();

        ASTNode expected = new IfThenASTNode(
                new BoolLiteralASTNode(true),
                List.of(
                    new ExternalFunctionCallASTNode(
                            "IO",
                            "writeLine",
                            List.of(
                                    new IdentifierASTNode("X")
                            ))
                )
        );
        ParseTreeToASTVisitor convert = new ParseTreeToASTVisitor();

        assertThat(convert.visitIfStmt(tree)).isEqualTo(expected);
    }
}
