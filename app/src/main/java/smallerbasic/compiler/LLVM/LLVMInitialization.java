package smallerbasic.compiler.LLVM;

import org.jetbrains.annotations.NotNull;
import smallerbasic.AST.ASTMonoidVisitor;
import smallerbasic.AST.nodes.ASTNode;
import smallerbasic.AST.nodes.BoolLiteralASTNode;
import smallerbasic.AST.nodes.NumberLiteralASTNode;
import smallerbasic.AST.nodes.StringLiteralASTNode;
import smallerbasic.compiler.ASTToString;
import smallerbasic.symbolTable.SymbolTable;
import smallerbasic.symbolTable.VarNameGenerator;

/**
 * Given a {@link ASTNode} it creates the LLVM code needed to initialize all its literals.
 */
class LLVMInitialization implements ASTMonoidVisitor<StringBuilder>, ASTToString {
    private final @NotNull SymbolTable symbols;
    private final @NotNull VarNameGenerator gen;

    public LLVMInitialization(@NotNull SymbolTable symbols, @NotNull VarNameGenerator gen) {
        this.symbols = symbols;
        this.gen = gen;
    }

    private final static @NotNull String NUMBER_SETTER = "@_SET_NUM_VALUE";
    private final static @NotNull String BOOL_SETTER = "@_SET_BOOL_VALUE";
    private final static @NotNull String STRING_SETTER = "@_SET_STR_VALUE";

    private final static @NotNull String TRUE = "i1 1";
    private final static @NotNull String FALSE = "i1 0";

    /**
     * Stores the pointer to the string literal in a new variable and sets the box contents with it.
     * @param n The string literal.
     * @return The LLVM code to initialize a string literal.
     */
    @Override
    public StringBuilder visit(StringLiteralASTNode n) {
        String text = n.getValue();
        String arrayType = "[" + (text.length() + 1) + " x i8]";
        String ptr = "%" + gen.newName();
        String gep = ptr + " = getelementptr "
                + arrayType + ", "
                + arrayType + "* @"
                + symbols.getBinding(n) + ".value, i32 0, i32 0";
        String setter = "call void " + STRING_SETTER + "(%struct.Boxed* @" + symbols.getBinding(n) + ", i8* " + ptr + ")";
        return new StringBuilder(gep + "\n" + setter + "\n");
    }

    @Override
    public StringBuilder visit(NumberLiteralASTNode n) {
        String text = Double.toString(n.getValue());
        return new StringBuilder("call void " + NUMBER_SETTER
                + "(%struct.Boxed* @" + symbols.getBinding(n) + ", double " + text + ")\n");
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
    public StringBuilder visit(BoolLiteralASTNode n) {
        return new StringBuilder("call void " + BOOL_SETTER + "(%struct.Boxed* @" + symbols.getBinding(n)
                + ", " + (n.getValue() ? TRUE : FALSE) + ")\n");
    }

    @Override
    public String run(@NotNull ASTNode n) {
        return n.accept(this).toString();
    }
}

