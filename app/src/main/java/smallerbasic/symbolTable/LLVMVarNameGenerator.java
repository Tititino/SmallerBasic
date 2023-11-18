package smallerbasic.symbolTable;

import org.jetbrains.annotations.NotNull;

/**
 * A class to generate unique names LLVM compatible names.
 */
public class LLVMVarNameGenerator implements VarNameGenerator {
    private @NotNull Integer name = 0;
    public @NotNull String newName() {
        String newName = name.toString();
        name++;
        return "v" + newName;
    }
}
