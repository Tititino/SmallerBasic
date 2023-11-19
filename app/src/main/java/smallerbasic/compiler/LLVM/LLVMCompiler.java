package smallerbasic.compiler.LLVM;

import org.jetbrains.annotations.NotNull;
import smallerbasic.AST.nodes.ASTNode;
import smallerbasic.compiler.Compiler;

/**
 * Compiles a tree to LLVM IR.
 */
public class LLVMCompiler implements Compiler {
    /**
     * Given a {@link ASTNode} the method outputs the corresponding LLVM code.
     * The compilation to LLVM is split into four phases:
     * <ul>
     *   <li> preallocation of variables and literals </li>
     *   <li> initialization of literals </li>
     *   <li> every statement not in a routine grouped together in {@code @main} </li>
     *   <li> every routine </li>
     * </ul>
     * And these are composed like
     * <pre>
     * {@code
     * <preallocation>
     * <routines>
     * define i32 @main() {
     *   <initialization>
     *   <statements>
     *   ret i32 0
     * }
     * }
     * </pre>
     *
     * @param root An AST.
     * @return The LLVM code corresponding to the tree.
     */
    @Override
    public String compile(@NotNull ASTNode root) {
        VarNameGenerator gen = new VarNameGenerator();
        SymbolTable symbolTable = new SymbolTable(root, gen);
        String prealloc = new LLVMPreallocation(symbolTable).run(root);
        String init     = new LLVMInitialization(symbolTable, gen).run(root);
        String routines = new LLVMSubRoutinePrinter(symbolTable, gen).run(root);
        String body     = new LLVMMainPrinter(symbolTable, gen).run(root);

        return "\n" +
                prealloc +
                "\n" +
                routines +
                "\ndefine i32 @main() {\n" +
                init +
                "\n" +
                body +
                "\nret i32 0\n}\n";
    }
}
