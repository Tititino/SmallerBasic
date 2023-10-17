package smallerbasic.AST.staticChecks;

import org.jetbrains.annotations.NotNull;
import smallerbasic.AST.ASTVisitor;
import smallerbasic.AST.nodes.*;

import java.util.*;

public abstract class ASTLabelScopeChecking {

    public void check(@NotNull ASTNode n) {
        n.accept(new ScopeVisitor());
    }
    public abstract void reportError(Collection<String> missingLabels, ASTNode where);

    private class ScopeVisitor implements  ASTVisitor<InAndOut> {

        private InAndOut visitStmts(List<? extends ASTNode> l) {
            return l.stream()
                    .map(x -> x.accept(this))
                    .reduce(InAndOut.empty(), InAndOut::compose);
        }

        @Override
        public InAndOut visit(AssStmtASTNode n) {
            return InAndOut.empty();
        }

        @Override
        public InAndOut visit(BinOpASTNode n) {
            return InAndOut.empty();
        }

        @Override
        public InAndOut visit(BoolLiteralASTNode n) {
            return InAndOut.empty();
        }

        @Override
        public InAndOut visit(ExternalFunctionCallASTNode n) {
            return InAndOut.empty();
        }

        @Override
        public InAndOut visit(ForLoopASTNode n) {
            return visitStmts(n.getBody());
        }

        @Override
        public InAndOut visit(GotoStmtASTNode n) {
            return new InAndOut(Collections.emptySet(), Set.of(n.getLabel()));
        }

        @Override
        public InAndOut visit(IdentifierASTNode n) {
            return InAndOut.empty();
        }

        @Override
        public InAndOut visit(IfThenASTNode n) {
            if (n.getFalseBody().isPresent())
                return visitStmts(n.getFalseBody().get()).compose(visitStmts(n.getTrueBody()));
            return visitStmts(n.getTrueBody());
        }

        @Override
        public InAndOut visit(LabelDeclASTNode n) {
            return new InAndOut(Set.of(n.getName()), Collections.emptySet());
        }

        @Override
        public InAndOut visit(NumberLiteralASTNode n) {
            return InAndOut.empty();
        }

        @Override
        public InAndOut visit(ProgramASTNode n) {
            InAndOut labels = visitStmts(n.getContents());
            if (!labels.check()) {
                Set<String> missing = new HashSet<>(labels.gotoLabels());
                missing.removeAll(labels.definedLabels());
                reportError(missing, n);
            }
            return labels;
        }

        @Override
        public InAndOut visit(RoutineCallASTNode n) {
            return InAndOut.empty();
        }

        @Override
        public InAndOut visit(RoutineDeclASTNode n) {
            InAndOut labels = visitStmts(n.getBody());


            if (!labels.check()) {
                Set<String> missing = new HashSet<>(labels.gotoLabels());
                missing.removeAll(labels.definedLabels());
                reportError(missing, n);
            }
            return InAndOut.empty();
        }

        @Override
        public InAndOut visit(StringLiteralASTNode n) {
            return InAndOut.empty();
        }

        @Override
        public InAndOut visit(WhileLoopASTNode n) {
            return InAndOut.empty();
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
            return definedLabels.containsAll(gotoLabels);
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
