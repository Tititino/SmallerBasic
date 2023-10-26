package smallerbasic.symbolTable;

import org.jetbrains.annotations.NotNull;
import smallerbasic.AST.ASTMonoidVisitor;
import smallerbasic.AST.nodes.*;

import java.util.*;

public class SymbolTable {

    private final @NotNull VarNameGenerator gen;
    private final @NotNull Map<ScopedName<HasSymbol>, String> bindings = new HashMap<>();

    public @NotNull String getBinding(@NotNull HasSymbol n) {
        return bindings.get(new ScopedName<>(n, Scope.TOPLEVEL));
    }
    public @NotNull String getBinding(@NotNull HasSymbol id, @NotNull Scope scope) {
        return bindings.get(new ScopedName<>(id, scope));
    }

    private void newBinding(@NotNull ScopedName<HasSymbol> id) {
        bindings.put(id, gen.newName());
    }

    public SymbolTable(@NotNull ASTNode node, @NotNull VarNameGenerator gen) {
        this.gen = gen;
        List<ScopedName<HasSymbol>> symbols = node.accept(new GetSymbols()).stream().toList();
        for (ScopedName<HasSymbol> id : symbols)
            newBinding(id);
    }

    private static class GetSymbols implements ASTMonoidVisitor<Set<ScopedName<HasSymbol>>> {

        private @NotNull Scope currentScope = Scope.TOPLEVEL;
        @Override
        public Set<ScopedName<HasSymbol>> empty() {
            return Collections.emptySet();
        }

        @Override
        public Set<ScopedName<HasSymbol>> compose(Set<ScopedName<HasSymbol>> o1, Set<ScopedName<HasSymbol>> o2) {
            Set<ScopedName<HasSymbol>> newSet = new HashSet<>(o1);
            newSet.addAll(o2);
            return newSet;
        }

        @Override
        public Set<ScopedName<HasSymbol>> visit(RoutineNameASTNode n) {
            return Set.of(new ScopedName<>(n, Scope.TOPLEVEL));
        }

        @Override
        public Set<ScopedName<HasSymbol>> visit(RoutineDeclASTNode n) {
            currentScope = new Scope(n.getName().getText());
            Set<ScopedName<HasSymbol>> body = n.getBody()
                    .stream()
                    .map(x -> x.accept(this))
                    .reduce(empty(), this::compose);
            currentScope = Scope.TOPLEVEL;
            return compose(Set.of(new ScopedName<>(n.getName(), Scope.TOPLEVEL)), body);
        }

        @Override
        public Set<ScopedName<HasSymbol>> visit(RoutineCallASTNode n) {
            return empty();
        }

        @Override
        public Set<ScopedName<HasSymbol>> visit(GotoStmtASTNode n) {
            return empty();
        }

        @Override
        public Set<ScopedName<HasSymbol>> visit(LabelNameASTNode n) {
            return Set.of(new ScopedName<>(n, currentScope));
        }
        @Override
        public Set<ScopedName<HasSymbol>> visit(NumberLiteralASTNode n) {
            return Set.of(new ScopedName<>(n, Scope.TOPLEVEL));
        }
        @Override
        public Set<ScopedName<HasSymbol>> visit(StringLiteralASTNode n) {
            return Set.of(new ScopedName<>(n, Scope.TOPLEVEL));
        }
        @Override
        public Set<ScopedName<HasSymbol>> visit(BoolLiteralASTNode n) {
            return Set.of(new ScopedName<>(n, Scope.TOPLEVEL));
        }
        @Override
        public Set<ScopedName<HasSymbol>> visit(IdentifierASTNode n) {
            return Set.of(new ScopedName<>(n, Scope.TOPLEVEL));
        }
    }
}
