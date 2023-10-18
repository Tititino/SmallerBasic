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

    private class ScopeVisitor implements ASTMonoidVisitor<InAndOut> {
        @Override
        public InAndOut empty() {
            return InAndOut.empty();
        }

        @Override
        public InAndOut compose(InAndOut o1, InAndOut o2) {
            return o1.compose(o2);
        }

        @Override
        public InAndOut visit(GotoStmtASTNode n) {
            return new InAndOut(Collections.emptySet(), Set.of(n.getLabel()));
        }

        @Override
        public InAndOut visit(LabelDeclASTNode n) {
            return new InAndOut(Set.of(n.getName()), Collections.emptySet());
        }

        @Override
        public InAndOut visit(ProgramASTNode n) {
            InAndOut labels = visitChildren(n.getContents());
            if (!labels.check()) {
                isOk = false;
                Set<String> missing = new HashSet<>(labels.gotoLabels());
                missing.removeAll(labels.definedLabels());
                reportError(missing, n);
            }
            return labels;
        }

        @Override
        public InAndOut visit(RoutineDeclASTNode n) {
            InAndOut labels = visitChildren(n.getBody());

            if (!labels.check()) {
                isOk = false;
                Set<String> missing = new HashSet<>(labels.gotoLabels());
                missing.removeAll(labels.definedLabels());
                reportError(missing, n);
            }
            return empty();
        }
    }

    private record InAndOut(@NotNull Set<String> definedLabels, @NotNull Set<String> gotoLabels) {

        public static @NotNull InAndOut empty() {
            return new InAndOut(Collections.emptySet(), Collections.emptySet());
        }

        public @NotNull InAndOut compose(@NotNull InAndOut other) {
            Set<String> newDLabels = new HashSet<>(this.definedLabels);
            Set<String> newGLabels = new HashSet<>(this.gotoLabels);
            newDLabels.addAll(other.definedLabels);
            newGLabels.addAll(other.gotoLabels);
            return new InAndOut(newDLabels, newGLabels);
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
