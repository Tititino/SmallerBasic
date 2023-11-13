package smallerbasic.AST.staticChecks;

import org.jetbrains.annotations.NotNull;
import smallerbasic.AST.ASTMonoidVisitor;
import smallerbasic.AST.nodes.*;

import java.util.*;

/**
 * This check verifies whether each variable has been initialized before its use.
 * It ignores completely array accesses as the indexes may depend on runtime values.
 * Does not check indexed of arrays on the left side of assignments.
 */
public class UninitializedVariableCheck extends AbstractCheck {

    private boolean isOk = true;
    @Override
    public boolean check(@NotNull ASTNode n) {
        isOk = true;
        // Creates an anonymous visitor to collect routine declarations.
        ASTMonoidVisitor<Set<RoutineDeclASTNode>> routines = new ASTMonoidVisitor<>() {
            @Override
            public Set<RoutineDeclASTNode> empty() {
                return Collections.emptySet();
            }
            @Override
            public Set<RoutineDeclASTNode> compose(Set<RoutineDeclASTNode> o1, Set<RoutineDeclASTNode> o2) {
                Set<RoutineDeclASTNode> newSet = new HashSet<>(o1);
                newSet.addAll(o2);
                return newSet;
            }
            @Override
            public Set<RoutineDeclASTNode> visit(RoutineDeclASTNode n) {
                return Set.of(n);
            }
        };
        Set<RoutineDeclASTNode> routineDecls = n.accept(routines);
        n.accept(new UninitializedVisitor(routineDecls));
        return isOk;
    }

    private void reportError(@NotNull ASTNode n, @NotNull String msg) {
        isOk = false;
        super.reporter.reportError(n, msg);
    }

    /**
     * This visitor collects defined variables in a set.
     * For each variable encountered in an expression, the visitor checks if this is contained in the set of defined names.
     * If it is not a warning is issued.
     */
    private class UninitializedVisitor implements ASTMonoidVisitor<Void> {

        /**
         * Since recursive routines are permitted, this flag is set when exploring a routine body to prevent an infinite loops.
         */
        private boolean recursing = false;
        /**
         * A map from routine names to their body.
         * A routine body is explored only when the routine is called.
         */
        private final @NotNull Map<RoutineNameASTNode, List<StatementASTNode>> decls = new HashMap<>();

        /**
         * A set of all variables with an assigned value.
         */
        private final @NotNull Set<VariableASTNode> setVars = new HashSet<>();

        private UninitializedVisitor(Collection<RoutineDeclASTNode> decls) {
            for (RoutineDeclASTNode r : decls)
                this.decls.put(r.getName(), r.getBody());
        }


        @Override
        public Void empty() {
            return null;
        }

        @Override
        public Void compose(Void o1, Void o2) {
            return empty();
        }

        @Override
        public Void visit(AssStmtASTNode n) {
            setVars.add(n.getVarName());
            n.getValue().accept(this);
            return empty();
        }

        @Override
        public Void visit(RoutineCallASTNode n) {
            if (!recursing) {
                recursing = true;
                visitChildren(decls.getOrDefault(n.getFunction(), Collections.emptyList()));
                recursing = false;
            }
            return empty();
        }

        /**
         * Visiting a routine is postponed to when the routine is called in the TOPLEVEL.
         */
        @Override
        public Void visit(RoutineDeclASTNode n) {
            return empty();
        }

        @Override
        public Void visit(ForLoopASTNode n) {
            setVars.add(n.getVarName());
            n.getStart().accept(this);
            n.getEnd().accept(this);
            n.getStep().accept(this);
            visitChildren(n.getBody());
            return empty();
        }

        @Override
        public Void visit(BinOpASTNode n) {
            n.getLeft().accept(this);
            n.getRight().accept(this);
            return empty();
        }

        @Override
        public Void visit(IdentifierASTNode n) {
            if (!setVars.contains(n))
                reportError(n, "*** UninitializedWarning: variable \""
                        + n.getName() + "\" may not have been initialized");
            return empty();
        }

        /**
         * Array identifiers are ignored since indexes may be dependant on runtime values.
         */
        @Override
        public Void visit(ArrayASTNode n) {
            // does not make sense to check arrays since indexes could be not known statically
            visitChildren(n.getIndexes());
            return empty();
        }
    }

}
