package smallerbasic;

import org.junit.jupiter.api.Test;
import smallerbasic.AST.nodes.ASTNode;
import smallerbasic.AST.staticChecks.ASTLabelScopeChecking;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static smallerbasic.CompilationUtils.*;

public class ASTChecksTest {
    @Test
    public void labelScopeCheckingTest() {
        ASTNode tree = clean(parse(lex("Goto label\nlabel:\n")));
        ASTLabelScopeChecking checkScope = new ASTLabelScopeChecking() {
            @Override
            public void reportError(Collection<String> missingLabels, ASTNode where) {
                throw new RuntimeException();
            }
        };

        assertThatNoException().isThrownBy(() -> checkScope.check(tree));
    }

    @Test
    public void fromGlobalToRoutineTest() {
        ASTNode tree = clean(parse(lex("Sub test\nlabel:\nEndSub\nGoto label\n")));

        ASTLabelScopeChecking checkScope = new ASTLabelScopeChecking() {
            @Override
            public void reportError(Collection<String> missingLabels, ASTNode where) {
                List<String> labels = new ArrayList<>(missingLabels.size());
                labels.addAll(missingLabels);

                throw new RuntimeException("label \"" + labels.get(0) + "\" not in scope");
            }
        };

        assertThatThrownBy(() -> checkScope.check(tree))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("label \"label\" not in scope");
    }

    @Test
    public void fromRoutineToGlobalTest() {
        ASTNode tree = clean(parse(lex("Sub test\nGoto label1\nEndSub\nlabel1:\n")));

        ASTLabelScopeChecking checkScope = new ASTLabelScopeChecking() {
            @Override
            public void reportError(Collection<String> missingLabels, ASTNode where) {
                List<String> labels = new ArrayList<>(missingLabels.size());
                labels.addAll(missingLabels);

                throw new RuntimeException("label \"" + labels.get(0) + "\" not in scope");
            }
        };

        assertThatThrownBy(() -> checkScope.check(tree))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("label \"label1\" not in scope");
    }
}
