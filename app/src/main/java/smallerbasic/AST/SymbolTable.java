package smallerbasic.AST;

import org.jetbrains.annotations.NotNull;
import smallerbasic.AST.nodes.*;

import javax.print.DocFlavor;
import java.util.*;

public class SymbolTable extends SymbolTableVisitor<ASTNode> {


    public SymbolTable(@NotNull ASTNode node, @NotNull VarNameGenerator gen) {
        super(node, gen);
    }
    protected @NotNull List<ASTNode> getAll(@NotNull ASTNode n) {
        return new ArrayList<>(n.accept(new GetSymbols()));
    }


    private static class GetSymbols implements ASTMonoidVisitor<Set<ASTNode>> {
        @Override
        public Set<ASTNode> empty() {
            return Collections.emptySet();
        }

        @Override
        public Set<ASTNode> compose(Set<ASTNode> o1, Set<ASTNode> o2) {
            Set<ASTNode> newSet = new HashSet<>(o1);
            newSet.addAll(o2);
            return newSet;
        }

        @Override
        public Set<ASTNode> visit(RoutineDeclASTNode n) {
            return n.getBody()
                    .stream()
                    .map(x -> x.accept(this))
                    .reduce(Set.of(n), this::compose);
        }
        @Override
        public Set<ASTNode> visit(LabelDeclASTNode n) {return Set.of(n);}
        @Override
        public Set<ASTNode> visit(NumberLiteralASTNode n) {return Set.of(n);}
        @Override
        public Set<ASTNode> visit(StringLiteralASTNode n) {return Set.of(n);}
        @Override
        public Set<ASTNode> visit(BoolLiteralASTNode n) {return Set.of(n);}
        @Override
        public Set<ASTNode> visit(IdentifierASTNode n) {
            return Set.of(n);
        }
    }
}
