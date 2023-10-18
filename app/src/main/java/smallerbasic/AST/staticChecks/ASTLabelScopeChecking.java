package smallerbasic.AST.staticChecks;

import org.jetbrains.annotations.NotNull;
import smallerbasic.AST.ASTMonoidVisitor;
import smallerbasic.AST.nodes.*;

import java.util.*;

public abstract class ASTLabelScopeChecking implements Check {

    private boolean isOk = true;

    public boolean check(@NotNull ASTNode n) {
        isOk = true;
        n.accept(new ScopeVisitor());
        return isOk;
    }
    public abstract void reportError(Collection<String> missingLabels, ASTNode where);

    private class ScopeVisitor implements ASTMonoidVisitor<DefAndRefLabels> {
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
            return new DefAndRefLabels(Collections.emptySet(), Set.of(n.getLabel()));
        }

        @Override
        public DefAndRefLabels visit(LabelDeclASTNode n) {
            return new DefAndRefLabels(Set.of(n.getName()), Collections.emptySet());
        }

        @Override
        public DefAndRefLabels visit(ProgramASTNode n) {
            DefAndRefLabels labels = visitChildren(n.getContents());
            if (!labels.check()) {
                isOk = false;
                Set<String> missing = new HashSet<>(labels.gotoLabels());
                missing.removeAll(labels.definedLabels());
                reportError(missing, n);
            }
            return labels;
        }

        @Override
        public DefAndRefLabels visit(RoutineDeclASTNode n) {
            DefAndRefLabels labels = visitChildren(n.getBody());

            if (!labels.check()) {
                isOk = false;
                Set<String> missing = new HashSet<>(labels.gotoLabels());
                missing.removeAll(labels.definedLabels());
                reportError(missing, n);
            }
            return empty();
        }
    }

    private record DefAndRefLabels(@NotNull Set<String> definedLabels, @NotNull Set<String> gotoLabels) {

        public static @NotNull ASTLabelScopeChecking.DefAndRefLabels empty() {
            return new DefAndRefLabels(Collections.emptySet(), Collections.emptySet());
        }

        public @NotNull ASTLabelScopeChecking.DefAndRefLabels compose(@NotNull ASTLabelScopeChecking.DefAndRefLabels other) {
            Set<String> newDLabels = new HashSet<>(this.definedLabels);
            Set<String> newGLabels = new HashSet<>(this.gotoLabels);
            newDLabels.addAll(other.definedLabels);
            newGLabels.addAll(other.gotoLabels);
            return new DefAndRefLabels(newDLabels, newGLabels);
        }

        public boolean check() {
            return this.definedLabels().containsAll(this.gotoLabels());
        }

        @Override
        public @NotNull Set<String> definedLabels() {
            return Collections.unmodifiableSet(definedLabels);
        }

        @Override
        public @NotNull Set<String> gotoLabels() {
            return Collections.unmodifiableSet(gotoLabels);
        }
    }
}
