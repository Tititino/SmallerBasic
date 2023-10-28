package smallerbasic.AST.staticChecks;

import org.antlr.v4.runtime.Token;
import org.jetbrains.annotations.NotNull;
import smallerbasic.AST.ASTMonoidVisitor;
import smallerbasic.AST.nodes.*;
import smallerbasic.AST.Scope;

import java.util.*;

/**
 * This check verifies whether a program defines a label more than once in the same scope.
 */
public class DoubleLabelCheck implements Check {

    @Override
    public boolean check(@NotNull ASTNode n) {
        boolean isOk = true;
        Map<LabelNameASTNode, Integer> labels = n.accept(new DoubleLabelVisitor());
        for (LabelNameASTNode s : labels.keySet())
            if (labels.get(s) > 1) {
                isOk = false;
                reportError(String.format(
                        "*** DoubleLabelError: label \"%s\" defined at line %d already defined in the same scope (%s)",
                        s.getText(),
                        s.getStartToken().map(Token::getLine).orElse(-1),
                        s.getScope()
                        )
                );
            }
        return isOk;
    }

    private class DoubleLabelVisitor implements ASTMonoidVisitor<Map<LabelNameASTNode, Integer>> {

        @Override
        public Map<LabelNameASTNode, Integer> empty() {
            return Collections.emptyMap();
        }

        @Override
        public Map<LabelNameASTNode, Integer> compose(Map<LabelNameASTNode, Integer> o1,
                                                                  Map<LabelNameASTNode, Integer> o2) {
            Map<LabelNameASTNode, Integer> newMap = new HashMap<>(o1);
            for (LabelNameASTNode key : o2.keySet())
                newMap.merge(key, o2.get(key), Integer::sum);
            return newMap;
        }

        @Override
        public Map<LabelNameASTNode, Integer> visit(LabelNameASTNode n) {
            return Map.of(n, 1);
        }

        @Override
        public Map<LabelNameASTNode, Integer> visit(GotoStmtASTNode n) {
            return empty();
        }
    }
}
