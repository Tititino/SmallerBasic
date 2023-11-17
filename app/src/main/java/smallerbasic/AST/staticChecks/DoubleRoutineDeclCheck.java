package smallerbasic.AST.staticChecks;

import org.jetbrains.annotations.NotNull;
import smallerbasic.AST.ASTMonoidVisitor;
import smallerbasic.AST.nodes.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * This check controls whether a program defines a routine more than once.
 */
public class DoubleRoutineDeclCheck extends AbstractCheck {
    @Override
    public boolean check(@NotNull ASTNode n) {
        boolean isOk = true;
        Map<RoutineNameASTNode, Integer> labels = n.accept(new DoubleRoutineNameVisitor());
        for (RoutineNameASTNode s : labels.keySet())
            if (labels.get(s) > 1) {
                isOk = false;
                super.reporter.reportError(s, String.format(
                                "*** DoubleRoutineDeclError: routine \"%s\" is redefined",
                                s.getText()
                        )
                );
            }
        return isOk;
    }

    private static class DoubleRoutineNameVisitor implements ASTMonoidVisitor<Map<RoutineNameASTNode, Integer>> {
        @Override
        public Map<RoutineNameASTNode, Integer> empty() {
            return Collections.emptyMap();
        }
        @Override
        public Map<RoutineNameASTNode, Integer> compose(Map<RoutineNameASTNode, Integer> o1,
                                                      Map<RoutineNameASTNode, Integer> o2) {
            Map<RoutineNameASTNode, Integer> newMap = new HashMap<>(o1);
            for (RoutineNameASTNode key : o2.keySet())
                newMap.merge(key, o2.get(key), Integer::sum);
            return newMap;
        }
        @Override
        public Map<RoutineNameASTNode, Integer> visit(RoutineNameASTNode n) {
            return Map.of(n, 1);
        }
        @Override
        public Map<RoutineNameASTNode, Integer> visit(RoutineCallASTNode n) {
            return empty();
        }
    }
}
