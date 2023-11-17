package smallerbasic.symbolTable;

import org.jetbrains.annotations.NotNull;

/**
 * Class for generating names.
 */
public interface VarNameGenerator {
    /**
     * Give me a new name.
     * @return a name, it is guaranteed that this name has not been returned before.
     */
    @NotNull String newName();
}
