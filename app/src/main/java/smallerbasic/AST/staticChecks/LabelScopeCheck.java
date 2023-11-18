package smallerbasic.AST.staticChecks;

import org.jetbrains.annotations.NotNull;
import smallerbasic.AST.ASTMonoidVisitor;
import smallerbasic.AST.nodes.*;

import java.util.*;

/**
 * This check verifies whether in a program each referenced label is also defined in the same scope.
 */
public class LabelScopeCheck extends AbstractCheck {
    public boolean check(@NotNull ASTNode n) {
        boolean isOk = true;
        DefAndRefLabels labels = n.accept(new ScopeVisitor());
        labels.gotoLabels().removeAll(labels.definedLabels());
        for (LabelNameASTNode l : labels.gotoLabels()) {
            isOk = false;
            super.reporter.reportError(l, String.format(
                            "*** LabelScopeError: the label \"%s\" referenced in this goto statement is not defined in this scope (%s)",
                            l.getText(),
                            l.getScope()
                    )
            );
        }
        return isOk;
    }

    private static class ScopeVisitor implements ASTMonoidVisitor<DefAndRefLabels> {
        @Override
        public DefAndRefLabels empty() {
            return DefAndRefLabels.empty();
        }

        @Override
        public DefAndRefLabels compose(DefAndRefLabels o1, DefAndRefLabels o2) {
            return o1.compose(o2);
        }

        @Override
        public DefAndRefLabels visit(GotoStmtASTNode n) {
            return new DefAndRefLabels(
                    Collections.emptySet(),
                    Set.of(n.getLabel())
            );
        }

        @Override
        public DefAndRefLabels visit(LabelDeclASTNode n) {
            return new DefAndRefLabels(
                    Set.of(n.getName()),
                    Collections.emptySet()
            );
        }
    }

    private record DefAndRefLabels(
            @NotNull Set<LabelNameASTNode> definedLabels,
            @NotNull Set<LabelNameASTNode> gotoLabels
    ) {
        public static @NotNull DefAndRefLabels empty() {
            return new DefAndRefLabels(Collections.emptySet(), Collections.emptySet());
        }

        public @NotNull DefAndRefLabels compose(@NotNull DefAndRefLabels other) {
            Set<LabelNameASTNode> newDLabels = new HashSet<>(this.definedLabels);
            Set<LabelNameASTNode> newGLabels = new HashSet<>(this.gotoLabels);
            newDLabels.addAll(other.definedLabels);
            newGLabels.addAll(other.gotoLabels);
            return new DefAndRefLabels(newDLabels, newGLabels);
        }
    }
}
