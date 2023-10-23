package smallerbasic.AST.staticChecks;

import org.antlr.v4.runtime.Token;
import org.jetbrains.annotations.NotNull;
import smallerbasic.AST.ASTMonoidVisitor;
import smallerbasic.AST.nodes.*;
import smallerbasic.symbolTable.Scope;
import smallerbasic.symbolTable.ScopedName;

import java.util.*;

public class DoubleLabelCheck implements Check {

    @Override
    public boolean check(@NotNull ASTNode n) {
        boolean isOk = true;
        Map<ScopedName<LabelNameASTNode>, Integer> labels = n.accept(new DoubleLabelVisitor());
        for (ScopedName<LabelNameASTNode> s : labels.keySet())
            if (labels.get(s) > 1) {
                isOk = false;
                reportError(String.format(
                        "*** DoubleLabelCheck: label \"%s\" defined at line %d already defined in the same scope (%s)",
                        s.node().getText(),
                        s.node().getStartToken().map(Token::getLine).orElse(-1),
                        s.scope()
                        )
                );
            }
        return isOk;
    }

    private class DoubleLabelVisitor implements ASTMonoidVisitor<Map<ScopedName<LabelNameASTNode>, Integer>> {

        private @NotNull Scope currentScope = Scope.TOPLEVEL;

        @Override
        public Map<ScopedName<LabelNameASTNode>, Integer> empty() {
            return Collections.emptyMap();
        }

        @Override
        public Map<ScopedName<LabelNameASTNode>, Integer> compose(Map<ScopedName<LabelNameASTNode>, Integer> o1,
                                                                  Map<ScopedName<LabelNameASTNode>, Integer> o2) {
            Map<ScopedName<LabelNameASTNode>, Integer> newMap = new HashMap<>(o1);
            for (ScopedName<LabelNameASTNode> key : o2.keySet())
                newMap.merge(key, o2.get(key), Integer::sum);
            return newMap;
        }

        @Override
        public Map<ScopedName<LabelNameASTNode>, Integer> visit(RoutineDeclASTNode n) {
            currentScope = new Scope(n.getName().getText());
            Map<ScopedName<LabelNameASTNode>, Integer> body = ASTMonoidVisitor.super.visit(n);
            currentScope = Scope.TOPLEVEL;
            return body;
        }

        @Override
        public Map<ScopedName<LabelNameASTNode>, Integer> visit(LabelNameASTNode n) {
            return Map.of(new ScopedName<>(n, currentScope), 1);
        }

        @Override
        public Map<ScopedName<LabelNameASTNode>, Integer> visit(GotoStmtASTNode n) {
            return empty();
        }
    }
}
