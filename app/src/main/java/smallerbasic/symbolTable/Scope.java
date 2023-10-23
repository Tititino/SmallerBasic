package smallerbasic.symbolTable;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class Scope {

    public static @NotNull Scope TOPLEVEL = new Scope();
    private @Nullable String name = null;

    private Scope() {}

    public Scope(@NotNull String name) {
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
}
