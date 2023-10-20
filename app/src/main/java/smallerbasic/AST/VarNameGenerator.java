package smallerbasic.AST;

import org.jetbrains.annotations.NotNull;

public class VarNameGenerator {

    private @NotNull Integer name = 0;
    public @NotNull String newName() {
        String newName = name.toString();
        name++;
        return newName;
    }
}
