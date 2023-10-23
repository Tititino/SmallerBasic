package smallerbasic.AST.staticChecks;

import org.jetbrains.annotations.NotNull;
import smallerbasic.AST.ASTMonoidVisitor;
import smallerbasic.AST.nodes.*;

import java.util.*;

public class ASTDoubleLabelChecking implements Check {

    private boolean isOk = true;
    @Override
    public boolean check(@NotNull ASTNode n) {
        isOk = true;
        n.accept(new DoubleLabelVisitor());
        return isOk;
    }

    private class DoubleLabelVisitor implements ASTMonoidVisitor<Map<String, Integer>> {

        @Override
        public Map<String, Integer> empty() {
            return Collections.emptyMap();
        }

        @Override
        public Map<String, Integer> compose(Map<String, Integer> o1, Map<String, Integer> o2) {
            Map<String, Integer> newMap = new HashMap<>(o1);
            for (String key : o2.keySet())
                newMap.merge(key, o2.get(key), Integer::sum);
            return newMap;
        }

        @Override
        public Map<String, Integer> visit(ProgramASTNode n) {
            Map<String, Integer> labelCounts = visitChildren(n.getContents());
            for (String key : labelCounts.keySet())
                if (labelCounts.get(key) > 1) {
                    isOk = false;
                    reportError("label \"" + key + "\" defined two times in the same scope");
                }
            return labelCounts;
        }

        @Override
        public Map<String, Integer> visit(RoutineDeclASTNode n) {
            Map<String, Integer> labelCounts = visitChildren(n.getBody());
            if (labelCounts.values().stream().anyMatch(x -> x > 1)) {
                isOk = false;
            }
            return empty();
        }

        @Override
        public Map<String, Integer> visit(LabelNameASTNode n) {
            return Map.of(n.getText(), 1);
        }
    }
}
