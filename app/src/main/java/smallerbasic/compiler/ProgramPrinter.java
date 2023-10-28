package smallerbasic.compiler;

import org.antlr.v4.runtime.Token;
import org.jetbrains.annotations.NotNull;
import smallerbasic.AST.ASTVisitor;
import smallerbasic.AST.nodes.*;
import smallerbasic.symbolTable.SymbolTable;
import smallerbasic.symbolTable.VarNameGenerator;

import java.util.List;
import java.util.Optional;

/**
 * This class takes a {@link ASTNode}, a {@link SymbolTable} and a {@link VarNameGenerator} and prints out the
 * LLVM IR corresponding to the tree.
 */
public class ProgramPrinter {

    /**
     * The symbol table.
     * This is queried to get the name associated with a certain node.
     */
    private final @NotNull SymbolTable symbols;
    private final @NotNull VarNameGenerator gen;

    /**
     *
     */
    private final @NotNull StringBuilder llvmProgram;

    public static String compile(@NotNull ASTNode root) {
        VarNameGenerator gen = new VarNameGenerator();
        SymbolTable symbols = new SymbolTable(root, gen);
        return new ProgramPrinter(symbols, gen, root).llvmProgram.toString();
    }

    private ProgramPrinter(@NotNull SymbolTable symbols,
                          @NotNull VarNameGenerator gen,
                          @NotNull ASTNode root) {
        this.symbols = symbols;
        this.gen = gen;
        llvmProgram = new StringBuilder();
        root.accept(new ProgramPrinterVisitor());
    }

    private class ProgramPrinterVisitor implements ASTVisitor<String> {

        private int lastLine = -1;
        private final static @NotNull String BOOL_GETTER = "@_GET_BOOL_VALUE";
        private final static @NotNull String NUMBER_SETTER = "@_SET_NUM_VALUE";
        private final static @NotNull String BOOL_SETTER = "@_SET_BOOL_VALUE";
        private final static @NotNull String STRING_SETTER = "@_SET_STR_VALUE";
        private final static @NotNull String OVERLOADED_PLUS = "@OVERLOADED_PLUS";
        private final static @NotNull String COPY_FUNC = "@_COPY";
        private final static @NotNull String NULL_VALUE = "%struct.Boxed { i3 0, i64 0 }";

        private final static @NotNull String GET_ARRAY_ELEMENT = "@_GET_ARRAY_ELEMENT";
        private final static @NotNull String TRUE = "i1 1";
        private final static @NotNull String FALSE = "i1 0";

        private void addLine(@NotNull String s) {
            llvmProgram.append(s).append("\n");
        }

        private void updateLineNumber(@NotNull ASTNode node) {
            Optional<Token> startToken = node.getStartToken();
            if (startToken.isPresent() && startToken.get().getLine() != lastLine) {
                int line = startToken.get().getLine();
                lastLine = line;
                addLine("store i32 " + line + ", ptr @line.number");
            }
        }

        @Override
        public String visit(AssStmtASTNode n) {
            updateLineNumber(n);
            String name = n.getVarName().accept(this);
            String rightSide = n.getValue().accept(this);
            addLine("call void " + COPY_FUNC + "(%struct.Boxed* " + name + ", %struct.Boxed* " + rightSide + ")");
            return null;
        }

        @Override
        public String visit(BinOpASTNode n) {
            updateLineNumber(n);
            String left = n.getLeft().accept(this);
            String right = n.getRight().accept(this);

            String res = "%" + gen.newName();
            String op = (n.getOp() == BinOpASTNode.BinOp.PLUS || n.getOp() == BinOpASTNode.BinOp.CONCAT)
                    ? OVERLOADED_PLUS
                    : "@" + n.getOp();
            addLine(res + " = alloca %struct.Boxed");
            addLine("call void " + op
                    + "(%struct.Boxed* " + res
                    + ", %struct.Boxed* " + left
                    + ", %struct.Boxed* " + right + ")");
            return res;
        }

        @Override
        public String visit(BoolLiteralASTNode n) {
            updateLineNumber(n);
            return "@" + symbols.getBinding(n);
        }

        @Override
        public String visit(ExternalFunctionCallASTNode n) {
            updateLineNumber(n);
            List<String> names = n.getArgs().stream().map(x -> x.accept(this)).toList();
            String newName = "%" + gen.newName();
            addLine(newName + " = alloca %struct.Boxed*");

            llvmProgram.append("call void @").append(n.getModule()).append(".").append(n.getFunction()).append("(");
            llvmProgram.append("%struct.Boxed* ").append(newName);
            for (String name : names)
                llvmProgram.append(", %struct.Boxed* ").append(name);
            llvmProgram.append(")\n");
            return newName;
        }

        @Override
        public String visit(ForLoopASTNode n) {
            updateLineNumber(n);
            String label = gen.newName();

            // VAR = START
            String var = n.getVarName().accept(this);
            String start = n.getStart().accept(this);
            addLine("call void " + COPY_FUNC + "(%struct.Boxed* " + var + ", %struct.Boxed* " + start + ")");

            addLine("br label %" + label + ".begin");
            addLine(label + ".begin:");

            // VAR < END
            String end = n.getEnd().accept(this);
            String cond = "%" + gen.newName();
            addLine(cond + " = alloca %struct.Boxed");
            addLine("call void @" + BinOpASTNode.BinOp.LEQ
                    + "(%struct.Boxed* " + cond
                    + ", %struct.Boxed* " + var
                    + ", %struct.Boxed* " + end + ")");
            String bool = "%" + gen.newName();
            addLine(bool + " = call i1 " + BOOL_GETTER + "(%struct.Boxed* " + cond + ")");
            addLine("br i1 " + bool + ", label %" + label + ".continue, label %" + label + ".end");
            addLine(label + ".continue:");
            n.getBody().forEach(x -> x.accept(this));

            // VAR += STEP
            String step = n.getStep().accept(this);
            String rightSide = "%" + gen.newName();
            addLine(rightSide + " = alloca %struct.Boxed");
            addLine("call void @" + BinOpASTNode.BinOp.PLUS
                    + "(%struct.Boxed* " + rightSide
                    + ", %struct.Boxed* " + var
                    + ", %struct.Boxed* " + step + ")");

            addLine("call void " + COPY_FUNC + "(%struct.Boxed* " + var + ", %struct.Boxed* " + rightSide + ")");

            addLine("br label %" + label + ".begin");
            addLine(label + ".end:");

            return null;
        }

        @Override
        public String visit(GotoStmtASTNode n) {
            updateLineNumber(n);
            String label = visit(n.getLabel());
            addLine("br label %" + label);
            return null;
        }

        @Override
        public String visit(IdentifierASTNode n) {
            updateLineNumber(n);
            return "@" + symbols.getBinding(n);
        }

        @Override
        public String visit(IfThenASTNode n) {
            updateLineNumber(n);
            String cond = n.getCondition().accept(this);
            String bool = "%" + gen.newName();
            String label = gen.newName();
            addLine(bool + " = call i1 " + BOOL_GETTER + "(%struct.Boxed* " + cond + ")");
            addLine("br i1 " + bool + ", label %" + label + ".true, label %" + label + ".false");
            addLine(label + ".true:");
            n.getTrueBody().forEach(x -> x.accept(this));
            addLine("br label %" + label + ".end");
            addLine(label + ".false:");
            n.getFalseBody().ifPresent(x -> x.forEach(y -> y.accept(this)));
            addLine("br label %" + label + ".end");
            addLine(label + ".end:");
            return null;
        }

        @Override
        public String visit(LabelDeclASTNode n) {
            updateLineNumber(n);
            String label = visit(n.getName());
            addLine(label + ":");
            return null;
        }

        @Override
        public String visit(NumberLiteralASTNode n) {
            updateLineNumber(n);
            return "@" + symbols.getBinding(n);
        }

        private void prealloc(CollectNodes c) {
            for (IdentifierASTNode id : c.getIdents())
                addLine("@" + symbols.getBinding(id) + " = global " + NULL_VALUE);
            for (NumberLiteralASTNode num : c.getNumberConstants())
                addLine("@" + symbols.getBinding(num) + " = global " + NULL_VALUE);
            for (BoolLiteralASTNode bool : c.getBoolConstants())
                addLine("@" + symbols.getBinding(bool) + " = global " + NULL_VALUE);
            for (StringLiteralASTNode str : c.getStringConstants()) {
                addLine("@" + symbols.getBinding(str) + " = global " + NULL_VALUE);
                String text = str.getValue();
                addLine("@" + symbols.getBinding(str)
                        + ".value = constant [" + (text.length() + 1)
                        + " x i8] c\"" + text + "\\00\"");
            }
        }

        private void initConstants(CollectNodes c) {
            for (StringLiteralASTNode str : c.getStringConstants()) {
                String text = str.getValue();
                String arrayType = "[" + (text.length() + 1) + " x i8]";
                String ptr = "%" + gen.newName();
                addLine(ptr + " = getelementptr "
                        + arrayType + ", "
                        + arrayType + "* @"
                        + symbols.getBinding(str) + ".value, i32 0, i32 0");
                addLine("call void " + STRING_SETTER + "(%struct.Boxed* @" + symbols.getBinding(str) + ", i8* " + ptr + ")");
            }
            for (NumberLiteralASTNode n : c.getNumberConstants()) {
                String text = Double.toString(n.getValue());
                addLine("call void " + NUMBER_SETTER + "(%struct.Boxed* @" + symbols.getBinding(n) + ", double " + text + ")");
            }
            for (BoolLiteralASTNode b : c.getBoolConstants()) {
                addLine("call void " + BOOL_SETTER + "(%struct.Boxed* @" + symbols.getBinding(b)
                        + ", " + (b.getValue() ? TRUE : FALSE) + ")");
            }
        }

        @Override
        public String visit(ProgramASTNode n) {
            CollectNodes constants = new CollectNodes(n);

            prealloc(constants);

            n.getContents()
                    .stream()
                    .filter(x -> x instanceof RoutineDeclASTNode)
                    .map(x -> (RoutineDeclASTNode) x)
                    .forEach(x -> x.accept(this));

            addLine("define i32 @main() {");
            initConstants(constants);
            updateLineNumber(n);

            n.getContents()
                    .stream()
                    .filter(x -> x instanceof StatementASTNode)
                    .map(x -> (StatementASTNode) x)
                    .forEach(x -> x.accept(this));

            addLine("ret i32 0\n}");
            return null;
        }

        @Override
        public String visit(LabelNameASTNode n) {
            updateLineNumber(n);
            return symbols.getBinding(n);
        }

        @Override
        public String visit(RoutineNameASTNode n) {
            return symbols.getBinding(n);
        }


        @Override
        public String visit(RoutineCallASTNode n) {
            String name = visit(n.getFunction());
            updateLineNumber(n);
            addLine("call void @" + name + "()");
            return null;
        }

        @Override
        public String visit(RoutineDeclASTNode n) {
            String name = visit(n.getName());
            addLine("define void @" + name + "() {");
            updateLineNumber(n);
            n.getBody().forEach(x -> x.accept(this));
            addLine("ret void\n}");
            return null;
        }

        @Override
        public String visit(StringLiteralASTNode n) {
            updateLineNumber(n);
            return "@" + symbols.getBinding(n);
        }

        @Override
        public String visit(WhileLoopASTNode n) {
            updateLineNumber(n);
            String label = gen.newName();
            String bool = "%" + gen.newName();
            addLine("br label %" + label + ".begin");  // strange llvm magic
            addLine(label + ".begin:");
            String cond = n.getCondition().accept(this);
            addLine(bool + " = call i1 " + BOOL_GETTER + "(%struct.Boxed* " + cond + ")");
            addLine("br i1 " + bool + ", label %" + label + ".continue, label %" + label + ".end");
            addLine(label + ".continue:");
            n.getBody().forEach(x -> x.accept(this));
            addLine("br label %" + label + ".begin");
            addLine(label + ".end:");
            return null;
        }

        @Override
        public String visit(UnaryMinusASTNode n) {
            updateLineNumber(n);
            String expr = n.getExpr().accept(this);
            String res = "%" + gen.newName();

            addLine(res + " = alloca %struct.Boxed");
            addLine("call void @UNARY_MINUS(%struct.Boxed* " + res
                    + ", %struct.Boxed* " + expr
                    + ")");
            return res;
        }

        @Override
        public String visit(ArrayASTNode n) {
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
                addLine(res + " = call %struct.Boxed* "
                        + GET_ARRAY_ELEMENT + "(%struct.Boxed* " + prev
                        + ", %struct.Boxed* " + index + ")");
                prev = res;
            }
            return res;
        }
    }
}