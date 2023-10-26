package smallerbasic.compiler;

import org.jetbrains.annotations.NotNull;
import smallerbasic.AST.ASTMonoidVisitor;
import smallerbasic.AST.nodes.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class CollectNodes {

    private final @NotNull Set<NumberLiteralASTNode> numberConstants = new HashSet<>();
    private final @NotNull Set<StringLiteralASTNode> stringConstants = new HashSet<>();
    private final @NotNull Set<BoolLiteralASTNode> boolConstants = new HashSet<>();
    private final @NotNull Set<IdentifierASTNode> idents = new HashSet<>();

    public CollectNodes(@NotNull ASTNode n) {
        n.accept(new CollectVisitor());
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
            return null;
        }

        @Override
        public Void visit(IdentifierASTNode n) {
            idents.add(n);
            return null;
        }

        @Override
        public Void visit(NumberLiteralASTNode n) {
            numberConstants.add(n);
            return null;
        }

        @Override
        public Void visit(StringLiteralASTNode n) {
            stringConstants.add(n);
            return null;
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
}
