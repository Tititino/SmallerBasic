package smallerbasic.AST.staticChecks;

import org.antlr.v4.runtime.Token;
import org.jetbrains.annotations.NotNull;
import smallerbasic.AST.ASTMonoidVisitor;
import smallerbasic.AST.nodes.*;
import smallerbasic.symbolTable.Scope;
import smallerbasic.symbolTable.ScopedName;

import java.util.*;

public class LabelScopeCheck implements Check {

    public boolean check(@NotNull ASTNode n) {
        boolean isOk = true;
        DefAndRefLabels labels = n.accept(new ScopeVisitor());
        labels.gotoLabels().removeAll(labels.definedLabels());
        for (ScopedName<LabelNameASTNode> l : labels.gotoLabels()) {
            isOk = false;
            reportError(String.format(
                            "*** LabelScopeCheck: label \"%s\" referenced at the goto of line %d is not defined in this scope (%s)",
                            l.node().getText(),
                            l.node().getStartToken().map(Token::getLine).orElse(-1),
                            l.scope()
                    )
            );
        }
        return isOk;
    }

    private class ScopeVisitor implements ASTMonoidVisitor<DefAndRefLabels> {
        private @NotNull Scope currentScope = Scope.TOPLEVEL;
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
                    Set.of(new ScopedName<>(n.getLabel(), currentScope))
            );
        }

        @Override
        public DefAndRefLabels visit(LabelDeclASTNode n) {
            return new DefAndRefLabels(
                    Set.of(new ScopedName<>(n.getName(), currentScope)),
                    Collections.emptySet()
            );
        }

        @Override
        public DefAndRefLabels visit(RoutineDeclASTNode n) {
            currentScope = new Scope(n.getName().getText());
            DefAndRefLabels labels = ASTMonoidVisitor.super.visit(n);
            currentScope = Scope.TOPLEVEL;
            return labels;
        }
    }

    private record DefAndRefLabels(@NotNull Set<ScopedName<LabelNameASTNode>> definedLabels, @NotNull Set<ScopedName<LabelNameASTNode>> gotoLabels) {

        public static @NotNull LabelScopeCheck.DefAndRefLabels empty() {
            return new DefAndRefLabels(Collections.emptySet(), Collections.emptySet());
        }

        public @NotNull LabelScopeCheck.DefAndRefLabels compose(@NotNull LabelScopeCheck.DefAndRefLabels other) {
            Set<ScopedName<LabelNameASTNode>> newDLabels = new HashSet<>(this.definedLabels);
            Set<ScopedName<LabelNameASTNode>> newGLabels = new HashSet<>(this.gotoLabels);
            newDLabels.addAll(other.definedLabels);
            newGLabels.addAll(other.gotoLabels);
            return new DefAndRefLabels(newDLabels, newGLabels);
        }
    }
}
