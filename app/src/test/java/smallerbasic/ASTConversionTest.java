package smallerbasic;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.Test;
import smallerbasic.AST.ParseTreeToASTVisitor;
import smallerbasic.AST.Scope;
import smallerbasic.AST.nodes.*;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static smallerbasic.CompilationUtils.*;

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
                new RoutineNameASTNode("test"),
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

    @Test
    public void programParsingTest() throws IOException {
        TokenStream lexedSource = lex(Paths.get("src/test/resources/test1.sb"));
        ParseTree parsedSource = parse(lexedSource).get();
        assertThatNoException().isThrownBy(() -> clean(parsedSource));
    }

    @Test
    public void routineAndStatementTest() {
        ASTNode tree = clean(parse(lex("Sub test\nlabel:\nEndSub\nGoto label\n")).get());
        RoutineNameASTNode name = new RoutineNameASTNode("test");
        ASTNode expected = new ProgramASTNode(List.of(
                new RoutineDeclASTNode(
                        name,
                        List.of(
                                new LabelDeclASTNode(new LabelNameASTNode("label", Scope.ofRoutine(name)))
                        )
                ),
                new GotoStmtASTNode(new LabelNameASTNode("label", Scope.TOPLEVEL))
        ));

        assertThat(tree).isEqualTo(expected);
    }

    @Test
    public void testTokenPosition() {
        ASTNode tree = clean(parse(lex("Sub test\nlabel:\nEndSub\nGoto label\n")).get());

        assertThat(tree.getStartToken().get().getText()).isEqualTo("Sub");
        assertThat(tree.getEndToken().get().getText()).isEqualTo("<EOF>");
    }

    @Test
    public void unaryMinusTest() {
        ASTNode tree = clean(parse(lex("A = -(B + -C)\n")).get());
        assertThat(tree).isEqualTo(
                new ProgramASTNode(
                        List.of(
                                new AssStmtASTNode(
                                        new IdentifierASTNode("A"),
                                        new UnaryMinusASTNode(
                                                new BinOpASTNode(
                                                        BinOpASTNode.BinOp.PLUS,
                                                        new IdentifierASTNode("B"),
                                                        new UnaryMinusASTNode(
                                                                new IdentifierASTNode("C")
                                                        )
                                                )
                                        )
                                )
                        )
                )
        );
    }
}
