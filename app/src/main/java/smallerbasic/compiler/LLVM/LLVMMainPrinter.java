package smallerbasic.compiler.LLVM;

import org.antlr.v4.runtime.Token;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import smallerbasic.AST.ASTVisitor;
import smallerbasic.AST.nodes.*;

import java.util.List;
import java.util.Optional;

/**
 * Given a {@link ASTNode} it creates the LLVM code for its statements.
 */
class LLVMMainPrinter implements ASTVisitor<String>, ASTToString {

    private @NotNull StringBuilder output = new StringBuilder();

    private void appendLine(@NotNull String s) {
        output.append(s).append("\n");
    }

    private void prependLine(@NotNull String s) {
        output = new StringBuilder(s).append("\n").append(output);
    }

    private final @NotNull SymbolTable symbols;
    private final @NotNull VarNameGenerator gen;

    public LLVMMainPrinter(@NotNull SymbolTable symbols,
                           @NotNull VarNameGenerator gen) {
        this.symbols = symbols;
        this.gen = gen;
    }

    /**
     * Last line printed.
     */
    private int lastLine = -1;
    private final static @NotNull String BOOL_GETTER = "@_GET_BOOL_VALUE";
    private final static @NotNull String COPY_FUNC = "@_COPY";
    private final static @NotNull String GET_ARRAY_ELEMENT = "@_GET_ARRAY_ELEMENT";

    /**
     * Line information is threaded inside the program using a global variable that is updated at each line change.
     * If {@link ASTNode}s do not have tokens associated with them the line will remain -1.
     * To avoid adding lots of {@code store}s for the same line number, this method checks that the new line
     * number is different from the last line printed {@see lastLine}.
     */
    private void updateLineNumber(@NotNull ASTNode node) {
        Optional<Token> startToken = node.getStartToken();
        if (startToken.isPresent() && startToken.get().getLine() != lastLine) {
            int line = startToken.get().getLine();
            lastLine = line;
            appendLine("store i32 " + line + ", ptr @line.number");
        }
    }

    @Override
    public @Nullable String visit(@NotNull AssStmtASTNode n) {
        updateLineNumber(n);
        String name = n.getVarName().accept(this);
        String rightSide = n.getValue().accept(this);
        appendLine("call void " + COPY_FUNC + "(%struct.Boxed* " + name + ", %struct.Boxed* " + rightSide + ")");
        return null;
    }

    @Override
    public @NotNull String visit(@NotNull BinOpASTNode n) {
        updateLineNumber(n);
        String left = n.getLeft().accept(this);
        String right = n.getRight().accept(this);

        String res = "%" + gen.newName();
        prependLine(res + " = alloca %struct.Boxed");
        appendLine("call void @" + n.getOp()
                + "(%struct.Boxed* " + res
                + ", %struct.Boxed* " + left
                + ", %struct.Boxed* " + right + ")");
        return res;
    }

    @Override
    public @NotNull String visit(@NotNull BoolLiteralASTNode n) {
        updateLineNumber(n);
        return "@" + symbols.getBinding(n);
    }

    @Override
    public @NotNull String visit(@NotNull ExternalFunctionCallASTNode n) {
        updateLineNumber(n);
        List<String> names = n.getArgs().stream().map(x -> x.accept(this)).toList();
        String newName = "%" + gen.newName();
        prependLine(newName + " = alloca %struct.Boxed*");

        output.append("call void @").append(n.getModule()).append(".").append(n.getFunction()).append("(");
        output.append("%struct.Boxed* ").append(newName);  // return value
        for (String name : names)
            output.append(", %struct.Boxed* ").append(name);
        output.append(")\n");
        return newName;
    }

    @Override
    public @Nullable String visit(@NotNull ForLoopASTNode n) {
        updateLineNumber(n);
        String label = gen.newName();

        // VAR = START
        String var = n.getVarName().accept(this);
        String start = n.getStart().accept(this);
        appendLine("call void " + COPY_FUNC + "(%struct.Boxed* " + var + ", %struct.Boxed* " + start + ")");

        appendLine("br label %" + label + ".begin");
        appendLine(label + ".begin:");

        // VAR <= END
        String end = n.getEnd().accept(this);
        String cond = "%" + gen.newName();
        prependLine(cond + " = alloca %struct.Boxed");
        appendLine("call void @" + BinOpASTNode.BinOp.LEQ
                + "(%struct.Boxed* " + cond
                + ", %struct.Boxed* " + var
                + ", %struct.Boxed* " + end + ")");
        String bool = "%" + gen.newName();
        appendLine(bool + " = call i1 " + BOOL_GETTER + "(%struct.Boxed* " + cond + ")");
        appendLine("br i1 " + bool + ", label %" + label + ".continue, label %" + label + ".end");
        appendLine(label + ".continue:");
        n.getBody().forEach(x -> x.accept(this));

        // VAR += STEP
        String step = n.getStep().accept(this);
        String rightSide = "%" + gen.newName();
        prependLine(rightSide + " = alloca %struct.Boxed");
        appendLine("call void @" + BinOpASTNode.BinOp.PLUS
                + "(%struct.Boxed* " + rightSide
                + ", %struct.Boxed* " + var
                + ", %struct.Boxed* " + step + ")");

        appendLine("call void " + COPY_FUNC + "(%struct.Boxed* " + var + ", %struct.Boxed* " + rightSide + ")");

        appendLine("br label %" + label + ".begin");
        appendLine(label + ".end:");

        return null;
    }

    @Override
    public @Nullable String visit(@NotNull GotoStmtASTNode n) {
        updateLineNumber(n);
        String label = visit(n.getLabel());
        appendLine("br label %" + label);
        return null;
    }

    @Override
    public @NotNull String visit(@NotNull IdentifierASTNode n) {
        updateLineNumber(n);
        return "@" + symbols.getBinding(n);
    }

    @Override
    public @Nullable String visit(@NotNull IfThenASTNode n) {
        updateLineNumber(n);
        String cond = n.getCondition().accept(this);
        String bool = "%" + gen.newName();
        String label = gen.newName();
        appendLine(bool + " = call i1 " + BOOL_GETTER + "(%struct.Boxed* " + cond + ")");
        appendLine("br i1 " + bool + ", label %" + label + ".true, label %" + label + ".false");
        appendLine(label + ".true:");
        n.getTrueBody().forEach(x -> x.accept(this));
        appendLine("br label %" + label + ".end");
        appendLine(label + ".false:");
        n.getFalseBody().ifPresent(x -> x.forEach(y -> y.accept(this)));
        appendLine("br label %" + label + ".end");
        appendLine(label + ".end:");
        return null;
    }

    @Override
    public @Nullable String visit(@NotNull LabelDeclASTNode n) {
        updateLineNumber(n);
        String label = visit(n.getName());
        appendLine("br label %" + label);
        appendLine(label + ":");
        return null;
    }

    @Override
    public @NotNull String visit(@NotNull NumberLiteralASTNode n) {
        updateLineNumber(n);
        return "@" + symbols.getBinding(n);
    }

    @Override
    public @Nullable String visit(@NotNull ProgramASTNode n) {
        n.getContents().forEach(x -> x.accept(this));
        return null;
    }

    @Override
    public @NotNull String visit(@NotNull LabelNameASTNode n) {
        updateLineNumber(n);
        return symbols.getBinding(n);
    }

    @Override
    public @NotNull String visit(@NotNull RoutineNameASTNode n) {
        return "@" + symbols.getBinding(n);
    }

    @Override
    public @Nullable String visit(@NotNull RoutineCallASTNode n) {
        String name = visit(n.getFunction());
        updateLineNumber(n);
        appendLine("call void " + name + "()");
        return null;
    }

    @Override
    public @Nullable String visit(@NotNull RoutineDeclASTNode n) {
        return null;
    }

    @Override
    public @NotNull String visit(@NotNull StringLiteralASTNode n) {
        updateLineNumber(n);
        return "@" + symbols.getBinding(n);
    }

    @Override
    public @Nullable String visit(@NotNull WhileLoopASTNode n) {
        updateLineNumber(n);
        String label = gen.newName();
        String bool = "%" + gen.newName();

        appendLine("br label %" + label + ".begin");  // strange llvm magic
        appendLine(label + ".begin:");
        String cond = n.getCondition().accept(this);
        appendLine(bool + " = call i1 " + BOOL_GETTER + "(%struct.Boxed* " + cond + ")");
        appendLine("br i1 " + bool + ", label %" + label + ".continue, label %" + label + ".end");
        appendLine(label + ".continue:");
        n.getBody().forEach(x -> x.accept(this));
        appendLine("br label %" + label + ".begin");
        appendLine(label + ".end:");
        return null;
    }

    @Override
    public @Nullable String visit(@NotNull UnaryMinusASTNode n) {
        updateLineNumber(n);
        String expr = n.getExpr().accept(this);
        String res = "%" + gen.newName();

        appendLine(res + " = alloca %struct.Boxed");
        appendLine("call void @UNARY_MINUS(%struct.Boxed* " + res
                + ", %struct.Boxed* " + expr
                + ")");
        return res;
    }

    @Override
    public @NotNull String visit(@NotNull ArrayASTNode n) {
        updateLineNumber(n);
        String name = visit(n.getName());
        List<String> indexes = n.getIndexes()
                .stream()
                .map(x -> x.accept(this))
                .toList();
        String res = "";
        String prev = name;
        for (String index : indexes) {
            res = "%" + gen.newName();
            appendLine(res + " = call %struct.Boxed* "
                    + GET_ARRAY_ELEMENT + "(%struct.Boxed* " + prev
                    + ", %struct.Boxed* " + index + ")");
            prev = res;
        }
        return res;
    }

    @Override
    public String run(@NotNull ASTNode n) {
        n.accept(this);
        return output.toString();
    }
}