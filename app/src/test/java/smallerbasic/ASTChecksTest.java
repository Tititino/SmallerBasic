package smallerbasic;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import smallerbasic.AST.nodes.ASTNode;
import smallerbasic.AST.staticChecks.DoubleLabelCheck;
import smallerbasic.AST.staticChecks.LabelScopeCheck;

import static org.assertj.core.api.Assertions.*;
import static smallerbasic.CompilationUtils.*;

public class ASTChecksTest {
    @Test
    public void labelScopeCheckingTest() {
        ASTNode tree = clean(parse(lex("Goto label\nlabel:\n")).get());
        LabelScopeCheck checkScope = new LabelScopeCheck() {
            @Override
            public void reportError(@NotNull ASTNode n, @NotNull String msg) {
                throw new RuntimeException();
            }
        };

        assertThat(checkScope.check(tree)).isTrue();
        assertThatNoException().isThrownBy(() -> checkScope.check(tree));
    }

    @Test
    public void fromGlobalToRoutineTest() {
        ASTNode tree = clean(parse(lex("Sub test\nlabel:\nEndSub\nGoto label\n")).get());

        LabelScopeCheck checkScope = new LabelScopeCheck();
        checkScope.setErrorReporter((x, y) -> {throw new RuntimeException();});

        assertThatThrownBy(() -> checkScope.check(tree))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    public void fromRoutineToGlobalTest() {
        ASTNode tree = clean(parse(lex("Sub test\nGoto label1\nEndSub\nlabel1:\n")).get());

        LabelScopeCheck checkScope = new LabelScopeCheck();
        checkScope.setErrorReporter((x, y) -> {throw new RuntimeException();});

        assertThatThrownBy(() -> checkScope.check(tree))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    public void doubleLabelTest() {
        ASTNode tree = clean(parse(lex("Sub test\nlabel:\nlabel:\nEndSub\n")).get());

        DoubleLabelCheck checkDoubles = new DoubleLabelCheck();
        checkDoubles.setErrorReporter((n, msg) -> {});

        assertThat(checkDoubles.check(tree)).isFalse();
    }

    @Test
    public void doubleLabelDifferentScopeTest() {
        ASTNode tree = clean(parse(lex("Sub test\nlabel:\nEndSub\nlabel:\n")).get());

        DoubleLabelCheck checkDoubles = new DoubleLabelCheck();

        assertThat(checkDoubles.check(tree)).isTrue();
    }
}
