package smallerbasic.compiler;

import org.jetbrains.annotations.NotNull;
import smallerbasic.AST.ASTMonoidVisitor;
import smallerbasic.AST.nodes.*;

import java.util.*;

/**
 * This class neatly separates different {@link ASTNode}s that need to be treated differently.
 * For example string constants need to have a box allocated and also to have the string allocated.
 */
class CollectNodes {

    private final @NotNull Set<NumberLiteralASTNode> numberConstants = new HashSet<>();
    private final @NotNull Set<StringLiteralASTNode> stringConstants = new HashSet<>();
    private final @NotNull Set<BoolLiteralASTNode> boolConstants = new HashSet<>();
    private final @NotNull Set<IdentifierASTNode> idents = new HashSet<>();
    private final @NotNull Set<RoutineDeclASTNode> decls = new HashSet<>();

    /**
     * In the case of statements the order is important so a list is used.
     */
    private final @NotNull List<StatementASTNode> main = new LinkedList<>();

    public CollectNodes(@NotNull ASTNode n) {
        n.accept(new NameAndConstantsCollectorVisitor());
        n.accept(new ProgramCollectorVisitor());
    }

    public @NotNull Set<RoutineDeclASTNode> getDecls() {
        return Collections.unmodifiableSet(decls);
    }

    public @NotNull List<StatementASTNode> getMain() {
        return Collections.unmodifiableList(main);
    }

    public @NotNull Set<NumberLiteralASTNode> getNumberConstants() {
        return Collections.unmodifiableSet(numberConstants);
    }

    public @NotNull Set<StringLiteralASTNode> getStringConstants() {
        return Collections.unmodifiableSet(stringConstants);
    }

    public @NotNull Set<BoolLiteralASTNode> getBoolConstants() {
        return Collections.unmodifiableSet(boolConstants);
    }

    public @NotNull Set<IdentifierASTNode> getIdents() {
        return Collections.unmodifiableSet(idents);
    }

    /**
     * Visits the AST and collect names and constants.
     */
    private class NameAndConstantsCollectorVisitor implements ASTMonoidVisitor<Void> {
        @Override
        public Void visit(BoolLiteralASTNode n) {
            boolConstants.add(n);
            return empty();
        }
        @Override
        public Void visit(IdentifierASTNode n) {
            idents.add(n);
            return empty();
        }
        @Override
        public Void visit(NumberLiteralASTNode n) {
            numberConstants.add(n);
            return empty();
        }
        @Override
        public Void visit(StringLiteralASTNode n) {
            stringConstants.add(n);
            return empty();
        }
        @Override
        public Void empty() {
            return null;
        }
        @Override
        public Void compose(Void o1, Void o2) {
            return null;
        }
    }

    /**
     * Visits the ProgramASTNode and returns builds a list of the statements that compose `main`, and a set of the declared routines.
     */
    private class ProgramCollectorVisitor implements ASTMonoidVisitor<Void> {

        @Override
        public Void empty() {
            return null;
        }

        @Override
        public Void compose(Void o1, Void o2) {
            return null;
        }

        @Override
        public Void visit(AssStmtASTNode n) {
            main.add(n);
            return empty();
        }

        @Override
        public Void visit(ExternalFunctionCallASTNode n) {
            main.add(n);
            return empty();
        }

        @Override
        public Void visit(ForLoopASTNode n) {
            main.add(n);
            return empty();
        }

        @Override
        public Void visit(GotoStmtASTNode n) {
            main.add(n);
            return empty();
        }

        @Override
        public Void visit(IfThenASTNode n) {
            main.add(n);
            return empty();
        }

        @Override
        public Void visit(LabelDeclASTNode n) {
            main.add(n);
            return empty();
        }

        /**
         * Since every other method does not propagate the recursive calls only the first level, the list of statements and declarations that the ProgramASTNode contains, is explored.
         */
        @Override
        public Void visit(ProgramASTNode n) {
            visitChildren(n.getContents());
            return empty();
        }

        @Override
        public Void visit(RoutineCallASTNode n) {
            main.add(n);
            return empty();
        }

        @Override
        public Void visit(RoutineDeclASTNode n) {
            decls.add(n);
            return empty();
        }

        @Override
        public Void visit(WhileLoopASTNode n) {
            main.add(n);
            return empty();
        }
    }
}
