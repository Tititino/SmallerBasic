package smallerbasic.compiler;

import org.jetbrains.annotations.NotNull;
import smallerbasic.AST.ASTMonoidVisitor;
import smallerbasic.AST.nodes.*;

import java.util.*;

/**
 * This class neatly separates different nodes in the AST that need to be treated differently.
 * For example string constants need to have a box allocated and also to have the string allocated.
 */
public class CollectNodes {

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
        n.accept(new CollectVisitor());
        n.accept(new ProgramVisitor());
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

    private class CollectVisitor implements ASTMonoidVisitor<Void> {
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

    private class ProgramVisitor implements ASTMonoidVisitor<Void> {

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
