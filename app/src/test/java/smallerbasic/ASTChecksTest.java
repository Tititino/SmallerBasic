package smallerbasic;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import smallerbasic.AST.nodes.ASTNode;
import smallerbasic.AST.nodes.LabelDeclASTNode;
import smallerbasic.AST.staticChecks.ASTDoubleLabelChecking;
import smallerbasic.AST.staticChecks.ASTLabelScopeChecking;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static smallerbasic.CompilationUtils.*;

public class ASTChecksTest {
    @Test
    public void labelScopeCheckingTest() {
        ASTNode tree = clean(parse(lex("Goto label\nlabel:\n")));
        ASTLabelScopeChecking checkScope = new ASTLabelScopeChecking() {
            @Override
            public void reportError(@NotNull String msg) {
                throw new RuntimeException();
            }
        };

        assertThat(checkScope.check(tree)).isTrue();
        assertThatNoException().isThrownBy(() -> checkScope.check(tree));
    }

    @Test
    public void fromGlobalToRoutineTest() {
        ASTNode tree = clean(parse(lex("Sub test\nlabel:\nEndSub\nGoto label\n")));

        ASTLabelScopeChecking checkScope = new ASTLabelScopeChecking() {
            @Override
            public void reportError(@NotNull String msg) {
                throw new RuntimeException(msg);
            }
        };

        assertThatThrownBy(() -> checkScope.check(tree))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("label \"label\" is used in a goto, but never declared in this scope");
    }

    @Test
    public void fromRoutineToGlobalTest() {
        ASTNode tree = clean(parse(lex("Sub test\nGoto label1\nEndSub\nlabel1:\n")));

        ASTLabelScopeChecking checkScope = new ASTLabelScopeChecking() {
            @Override
            public void reportError(@NotNull String msg) {
                throw new RuntimeException(msg);
            }
        };

        assertThatThrownBy(() -> checkScope.check(tree))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("label \"label1\" is used in a goto, but never declared in this scope");
    }

    @Test
    public void doubleLabelTest() {
        ASTNode tree = clean(parse(lex("Sub test\nlabel:\nlabel:\nEndSub\n")));

        ASTDoubleLabelChecking checkDoubles = new ASTDoubleLabelChecking();

        assertThat(checkDoubles.check(tree)).isFalse();
    }

    @Test
    public void doubleLabelDifferentScopeTest() {
        ASTNode tree = clean(parse(lex("Sub test\nlabel:\nEndSub\nlabel:\n")));

        ASTDoubleLabelChecking checkDoubles = new ASTDoubleLabelChecking();

        assertThat(checkDoubles.check(tree)).isTrue();
    }

    @Test
    @Disabled
    public void symbolTableTest() {
        ASTNode tree = clean(parse(lex("""
                       For A = 1 To 10
                          For B = 1 To 10
                            C = 3 * A
                            Goto fine
                          EndFor
                       EndFor
                       B = C + B
                       fine:
                       D = 4
                       """)));

        VarNameGenerator gen = mock(VarNameGenerator.class);
        SymbolTable symbols = new SymbolTable(tree, gen);

        /*
        assertThat(symbols.getSymbols(IdentifierASTNode.class)).containsExactlyInAnyOrder(
                new IdentifierASTNode("A"),
                new IdentifierASTNode("B"),
                new IdentifierASTNode("C"),
                new IdentifierASTNode("D")
        );
         */
    }

    @Test
    @Disabled
    public void doubleLabelDifferentScopeSymbolTableTest() {
        ASTNode tree = clean(parse(lex("""
                       Sub test
                          label:
                          A = 1
                       EndSub
                       label:
                       A = 2
                       """)));

        SymbolTable symbols = new SymbolTable(tree, new VarNameGenerator());

        assertThat(symbols.getSymbols(LabelDeclASTNode.class)).hasSize(2);
    }
}
