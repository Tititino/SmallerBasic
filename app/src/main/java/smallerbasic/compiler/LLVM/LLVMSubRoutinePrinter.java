package smallerbasic.compiler.LLVM;

import org.jetbrains.annotations.NotNull;
import smallerbasic.AST.ASTMonoidVisitor;
import smallerbasic.AST.nodes.ASTNode;
import smallerbasic.AST.nodes.RoutineDeclASTNode;
import smallerbasic.compiler.ASTToString;
import smallerbasic.symbolTable.SymbolTable;
import smallerbasic.symbolTable.VarNameGenerator;

/**
 * Given a {@link ASTNode} it creates the LLVM code for all its subroutines.
 */
class LLVMSubRoutinePrinter implements ASTMonoidVisitor<StringBuilder>, ASTToString {

    private final @NotNull SymbolTable symbols;
    private final @NotNull VarNameGenerator gen;

    public LLVMSubRoutinePrinter(@NotNull SymbolTable symbols, @NotNull VarNameGenerator gen) {
        this.symbols = symbols;
        this.gen = gen;
    }

    @Override
    public StringBuilder empty() {
        return new StringBuilder();
    }

    @Override
    public StringBuilder compose(StringBuilder o1, StringBuilder o2) {
        return o1.append(o2);
    }

    @Override
    public StringBuilder visit(RoutineDeclASTNode n) {
        String name = symbols.getBinding(n.getName());
        String signature = "define void @" + name + "() {\n";
        String body = n.getBody().stream().map(stmt ->
                    new LLVMMainPrinter(symbols, gen).run(stmt)
                )
                .reduce("", (acc, x) -> acc + x);
        String end = "ret void\n}\n";
        return new StringBuilder(signature).append(body).append(end);
    }

    @Override
    public String run(@NotNull ASTNode n) {
        return n.accept(this).toString();
    }
}
