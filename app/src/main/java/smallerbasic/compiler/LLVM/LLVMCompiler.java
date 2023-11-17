package smallerbasic.compiler.LLVM;

import org.jetbrains.annotations.NotNull;
import smallerbasic.AST.nodes.ASTNode;
import smallerbasic.compiler.Compiler;
import smallerbasic.symbolTable.LLVMVarNameGenerator;
import smallerbasic.symbolTable.SymbolTable;
import smallerbasic.symbolTable.VarNameGenerator;

/**
 * Compiles a tree to LLVM.
 */
public class LLVMCompiler implements Compiler {
    /**
     * Given a {@link ASTNode} the method outputs the corresponding LLVM code.
     * @param root An AST.
     * @return The LLVM code corresponding to the tree.
     */
    @Override
    public String compile(@NotNull ASTNode root) {
        VarNameGenerator gen = new LLVMVarNameGenerator();
        SymbolTable symbolTable = new SymbolTable(root, gen);
        String prealloc = new LLVMPreallocation(symbolTable).run(root);
        String init     = new LLVMInitialization(symbolTable, gen).run(root);
        String routines = new LLVMSubRoutinePrinter(symbolTable, gen).run(root);
        String body     = new LLVMMainPrinter(symbolTable, gen).run(root);

        return prealloc +
                "\n" +
                routines +
                "\ndefine i32 @main() {\n" +
                init +
                "\n" +
                body +
                "\n}\n";
    }
}
