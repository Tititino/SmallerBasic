package smallerbasic.compiler.LLVM;

import org.jetbrains.annotations.NotNull;
import smallerbasic.AST.ASTMonoidVisitor;
import smallerbasic.AST.nodes.*;
import smallerbasic.compiler.ASTToString;
import smallerbasic.symbolTable.SymbolTable;

/**
 * Given an {@link ASTNode} it creates the LLVM code needed to preallocate all its variables and literals.
 */
class LLVMPreallocation implements ASTMonoidVisitor<StringBuilder>, ASTToString {
    private final @NotNull SymbolTable symbols;

    public LLVMPreallocation(@NotNull SymbolTable symbols) {
        this.symbols = symbols;
    }

    private final static @NotNull String NULL_VALUE = "%struct.Boxed { i3 0, i64 0 }";

    @Override
    public StringBuilder empty() {
        return new StringBuilder();
    }

    @Override
    public StringBuilder compose(StringBuilder o1, StringBuilder o2) {
        return o1.append(o2);
    }

    @Override
    public StringBuilder visit(BoolLiteralASTNode n) {
        return new StringBuilder("@" + symbols.getBinding(n) + " = global " + NULL_VALUE + "\n");
    }

    @Override
    public StringBuilder visit(IdentifierASTNode n) {
        return new StringBuilder("@" + symbols.getBinding(n) + " = global " + NULL_VALUE + "\n");
    }

    @Override
    public StringBuilder visit(NumberLiteralASTNode n) {
        return new StringBuilder("@" + symbols.getBinding(n) + " = global " + NULL_VALUE + "\n");
    }

    @Override
    public StringBuilder visit(StringLiteralASTNode n) {
        return new StringBuilder("@" + symbols.getBinding(n) + " = global " + NULL_VALUE + "\n");
    }

    @Override
    public String run(@NotNull ASTNode n) {
        return n.accept(this).toString();
    }
}
