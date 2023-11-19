package smallerbasic.compiler.LLVM;

import org.jetbrains.annotations.NotNull;
import smallerbasic.AST.ASTMonoidVisitor;
import smallerbasic.AST.nodes.*;

import java.util.HashSet;
import java.util.Set;

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

    private final @NotNull Set<ASTNode> seen = new HashSet<>();

    /**
     * Preallocate the space to hold the {@code %struct.Boxed} corresponding to this bool literal.
     * @param n The boolean literal.
     * @return Outputs LLVM IR code to allocate space for a single bool literal.
     */
    @Override
    public StringBuilder visit(BoolLiteralASTNode n) {
        if (seen.contains(n))
            return empty();
        seen.add(n);
        return new StringBuilder("@" + symbols.getBinding(n) + " = global " + NULL_VALUE + "\n");
    }

    /**
     * Preallocate the space to hold the {@code %struct.Boxed} corresponding to global variable.
     * @param n The gobal variable.
     * @return Outputs LLVM IR code to allocate space for a single global variable.
     */
    @Override
    public StringBuilder visit(IdentifierASTNode n) {
        if (seen.contains(n))
            return empty();
        seen.add(n);
        return new StringBuilder("@" + symbols.getBinding(n) + " = global " + NULL_VALUE + "\n");
    }

    /**
     * Preallocate the space to hold the {@code %struct.Boxed} corresponding to this number literal.
     * @param n The number literal.
     * @return Outputs LLVM IR code to allocate space for a single number literal.
     */
    @Override
    public StringBuilder visit(NumberLiteralASTNode n) {
        if (seen.contains(n))
            return empty();
        seen.add(n);
        return new StringBuilder("@" + symbols.getBinding(n) + " = global " + NULL_VALUE + "\n");
    }

    /**
     * Preallocate the space to hold the {@code %struct.Boxed}, and the space for the string.
     * @param n The string literal.
     * @return The LLVM code to allocate the space for them.
     */
    @Override
    public StringBuilder visit(StringLiteralASTNode n) {
        if (seen.contains(n))
            return empty();
        seen.add(n);
        String text = n.getValue();
        return new StringBuilder("@" + symbols.getBinding(n) + " = global " + NULL_VALUE + "\n")
                .append("@" + symbols.getBinding(n) + ".value = constant [" + (text.length() + 1) + " x i8] c\"" + text + "\\00\"\n");
    }

    @Override
    public String run(@NotNull ASTNode n) {
        return n.accept(this).toString();
    }
}
