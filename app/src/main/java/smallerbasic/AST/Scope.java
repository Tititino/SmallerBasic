package smallerbasic.AST;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import smallerbasic.AST.nodes.RoutineNameASTNode;

import java.util.Objects;

/**
 * A class representing the scope.
 * Since in SmallerBasic all variables are global, scope is only applied to labels.
 * And since subroutines cannot be nested, only two types of scopes are needed:
 *   - top-level
 *   - inside a routine
 */
public class Scope {

    public static @NotNull Scope TOPLEVEL = new Scope();
    private @Nullable RoutineNameASTNode name = null;

    private Scope() {}

    public static Scope ofRoutine(@NotNull RoutineNameASTNode name) {
        return new Scope(name);
    }

    private Scope(@NotNull RoutineNameASTNode name) {
        Objects.requireNonNull(name);
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Scope scope = (Scope) o;
        return Objects.equals(name, scope.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return (Objects.isNull(name) ? "TOPLEVEL" : name.getText());
    }
}
