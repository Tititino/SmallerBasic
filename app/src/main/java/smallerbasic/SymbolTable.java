package smallerbasic;

import org.jetbrains.annotations.NotNull;
import smallerbasic.AST.ASTMonoidVisitor;
import smallerbasic.AST.nodes.*;

import java.util.*;

public class SymbolTable {

    private final @NotNull VarNameGenerator gen;
    private final @NotNull List<ScopedName> symbols;
    private final @NotNull Map<ScopedName, String> bindings = new HashMap<>();

    public @NotNull String getBinding(@NotNull ASTNode n) {
        return bindings.get(new ScopedName(n, Scope.TOPLEVEL));
    }
    public @NotNull String getBinding(@NotNull ASTNode id, @NotNull Scope scope) {
        return bindings.get(new ScopedName(id, scope));
    }

    private void newBinding(@NotNull ScopedName id) {
        bindings.put(id, gen.newName());
    }

    public SymbolTable(@NotNull ASTNode node, @NotNull VarNameGenerator gen) {
        this.gen = gen;
        this.symbols = node.accept(new GetSymbols()).stream().toList();
        for (ScopedName id : symbols)
            newBinding(id);
    }
    public @NotNull List<ScopedName> getSymbols() {
        return symbols;
    }
    public <T extends ASTNode> @NotNull List<? extends T> getSymbols(Class<? extends T> c) {
        return symbols
                .stream()
                .map(ScopedName::node)
                .filter(c::isInstance)
                .map(c::cast)
                .toList();
    }

    private static class GetSymbols implements ASTMonoidVisitor<Set<ScopedName>> {

        private @NotNull Scope currentScope = Scope.TOPLEVEL;
        @Override
        public Set<ScopedName> empty() {
            return Collections.emptySet();
        }

        @Override
        public Set<ScopedName> compose(Set<ScopedName> o1, Set<ScopedName> o2) {
            Set<ScopedName> newSet = new HashSet<>(o1);
            newSet.addAll(o2);
            return newSet;
        }

        @Override
        public Set<ScopedName> visit(RoutineDeclASTNode n) {
            ScopedName functionName = new ScopedName(n, Scope.TOPLEVEL);
            currentScope = new Scope(n.getName());
            Set<ScopedName> body = n.getBody()
                    .stream()
                    .map(x -> x.accept(this))
                    .reduce(Set.of(functionName), this::compose);
            currentScope = Scope.TOPLEVEL;
            return body;
        }
        @Override
        public Set<ScopedName> visit(LabelDeclASTNode n) {
            return Set.of(new ScopedName(n, currentScope));
        }
        @Override
        public Set<ScopedName> visit(NumberLiteralASTNode n) {
            return Set.of(new ScopedName(n, Scope.TOPLEVEL));
        }
        @Override
        public Set<ScopedName> visit(StringLiteralASTNode n) {
            return Set.of(new ScopedName(n, Scope.TOPLEVEL));
        }
        @Override
        public Set<ScopedName> visit(BoolLiteralASTNode n) {
            return Set.of(new ScopedName(n, Scope.TOPLEVEL));
        }
        @Override
        public Set<ScopedName> visit(IdentifierASTNode n) {
            return Set.of(new ScopedName(n, Scope.TOPLEVEL));
        }
    }
}
