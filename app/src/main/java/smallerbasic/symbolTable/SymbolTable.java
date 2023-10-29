package smallerbasic.symbolTable;

import org.jetbrains.annotations.NotNull;
import smallerbasic.AST.ASTMonoidVisitor;
import smallerbasic.AST.nodes.*;

import java.util.*;

public class SymbolTable {

    private final @NotNull VarNameGenerator gen;
    private final @NotNull Map<HasSymbol, String> bindings = new HashMap<>();

    public @NotNull String getBinding(@NotNull HasSymbol n) {
        return bindings.get(n);
    }
    private void newBinding(@NotNull HasSymbol id) {
        bindings.put(id, gen.newName());
    }

    public SymbolTable(@NotNull ASTNode node, @NotNull VarNameGenerator gen) {
        this.gen = gen;
        List<HasSymbol> symbols = node.accept(new GetSymbols()).stream().toList();
        for (HasSymbol id : symbols)
            newBinding(id);
    }

    private static class GetSymbols implements ASTMonoidVisitor<Set<HasSymbol>> {
        @Override
        public Set<HasSymbol> empty() {
            return Collections.emptySet();
        }

        @Override
        public Set<HasSymbol> compose(Set<HasSymbol> o1, Set<HasSymbol> o2) {
            Set<HasSymbol> newSet = new HashSet<>(o1);
            newSet.addAll(o2);
            return newSet;
        }

        @Override
        public Set<HasSymbol> visit(RoutineNameASTNode n) {
            return Set.of(n);
        }

        @Override
        public Set<HasSymbol> visit(RoutineDeclASTNode n) {
            Set<HasSymbol> body = visitChildren(n.getBody());
            return compose(visit(n.getName()), body);
        }

        @Override
        public Set<HasSymbol> visit(RoutineCallASTNode n) {
            return empty();
        }
        @Override
        public Set<HasSymbol> visit(GotoStmtASTNode n) {
            return empty();
        }
        @Override
        public Set<HasSymbol> visit(LabelNameASTNode n) {
            return Set.of(n);
        }
        @Override
        public Set<HasSymbol> visit(NumberLiteralASTNode n) {
            return Set.of(n);
        }
        @Override
        public Set<HasSymbol> visit(StringLiteralASTNode n) {
            return Set.of(n);
        }
        @Override
        public Set<HasSymbol> visit(BoolLiteralASTNode n) {
            return Set.of(n);
        }
        @Override
        public Set<HasSymbol> visit(IdentifierASTNode n) {
            return Set.of(n);
        }
    }
}
