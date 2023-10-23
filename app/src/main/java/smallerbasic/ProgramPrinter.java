package smallerbasic;

import org.jetbrains.annotations.NotNull;
import smallerbasic.AST.ASTVisitor;
import smallerbasic.AST.nodes.*;
import smallerbasic.symbolTable.Scope;
import smallerbasic.symbolTable.SymbolTable;
import smallerbasic.symbolTable.VarNameGenerator;

import java.util.List;

public class ProgramPrinter {

    private final @NotNull SymbolTable symbols;
    private final @NotNull VarNameGenerator gen;

    private @NotNull StringBuilder llvmProgram = new StringBuilder();

    public ProgramPrinter(@NotNull SymbolTable symbols,
                          @NotNull VarNameGenerator gen) {
        this.symbols = symbols;
        this.gen = gen;
    }

    public @NotNull String compile(@NotNull ASTNode n) {
        llvmProgram = new StringBuilder();
        n.accept(new ProgramPrinterVisitor());
        return llvmProgram.toString();
    }

    private class ProgramPrinterVisitor implements ASTVisitor<String> {

        private @NotNull Scope currentScope = Scope.TOPLEVEL;
        private final static @NotNull String BOOL_GETTER = "@_GET_BOOL_VALUE";
        private final static @NotNull String NUMBER_SETTER = "@_SET_NUM_VALUE";
        private final static @NotNull String BOOL_SETTER = "@_SET_BOOL_VALUE";
        private final static @NotNull String STRING_SETTER = "@_SET_STR_VALUE";
        private final static @NotNull String COPY_FUNC = "@_COPY";

        private void addLine(@NotNull String s) {
            llvmProgram.append(s).append("\n");
        }

        @Override
        public String visit(AssStmtASTNode n) {
            String name = n.getVarName().accept(this);
            String rightSide = n.getValue().accept(this);
            addLine("call void " + COPY_FUNC + "(%struct.Boxed* " + name + ", %struct.Boxed* " + rightSide + ")");
            return null;
        }

        @Override
        public String visit(BinOpASTNode n) {
            String left = n.getLeft().accept(this);
            String right = n.getRight().accept(this);

            String res = "%" + gen.newName();
            addLine(res + " = alloca %struct.Boxed");
            addLine("call void @" + n.getOp()
                    + "(%struct.Boxed* " + res
                    + ", %struct.Boxed* " + left
                    + ", %struct.Boxed* " + right + ")");
            return res;
        }

        @Override
        public String visit(BoolLiteralASTNode n) {
            return "@" + symbols.getBinding(n);
        }

        @Override
        public String visit(ExternalFunctionCallASTNode n) {
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
            String label = gen.newName();
            (new AssStmtASTNode(n.getVarName(), n.getStart())).accept(this);
            addLine("br label %" + label + ".begin");
            addLine(label + ".begin:");

            String cond = (new BinOpASTNode(BinOpASTNode.BinOp.LT, n.getVarName(), n.getEnd())).accept(this);
            String bool = "%" + gen.newName();
            addLine(bool + " = call i1 " + BOOL_GETTER + "(%struct.Boxed* " + cond + ")");
            addLine("br i1 " + bool + ", label %" + label + ".continue, label %" + label + ".end");
            addLine(label + ".continue:");
            n.getBody().forEach(x -> x.accept(this));

            (new AssStmtASTNode(
                    n.getVarName(),
                    new BinOpASTNode(
                            BinOpASTNode.BinOp.PLUS,
                            n.getVarName(),
                            n.getStep()
                    )
            )).accept(this);

            addLine("br label %" + label + ".begin");
            addLine(label + ".end:");

            return null;
        }

        @Override
        public String visit(GotoStmtASTNode n) {
            addLine("br label %" + symbols.getBinding(n.getLabel(), currentScope));
            return null;
        }

        @Override
        public String visit(IdentifierASTNode n) {
            return "@" + symbols.getBinding(n);
        }

        @Override
        public String visit(IfThenASTNode n) {
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
            addLine(symbols.getBinding(n.getName(), currentScope) + ":\n");
            return null;
        }

        @Override
        public String visit(NumberLiteralASTNode n) {
            return "@" + symbols.getBinding(n);
        }

        @Override
        public String visit(ProgramASTNode n) {
            llvmProgram.append("; variables\n");
            for (IdentifierASTNode id : symbols.getSymbols(IdentifierASTNode.class))
                addLine("@" + symbols.getBinding(id) + " = global %struct.Boxed { i2 0, i64 0 }");

            llvmProgram.append("; boxed constants\n");
            for (LiteralASTNode lit : symbols.getSymbols(LiteralASTNode.class))
                addLine("@" + symbols.getBinding(lit) + " = global %struct.Boxed { i2 0, i64 0 }");

            llvmProgram.append("; string constants\n");
            for (StringLiteralASTNode lit : symbols.getSymbols(StringLiteralASTNode.class)) {
                String text = lit.getValue();
                addLine("@" + symbols.getBinding(lit)
                        + ".value = constant [" + (text.length() + 1)
                        + " x i8] c\"" + text + "\\00\"");
            }

            n.getContents()
                    .stream()
                    .filter(x -> x instanceof RoutineDeclASTNode)
                    .map(x -> (RoutineDeclASTNode) x)
                    .forEach(x -> x.accept(this));

            addLine("define i32 @main() {");
            for (LiteralASTNode lit : symbols.getSymbols(LiteralASTNode.class))
                prealloc(lit);

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
            return null;
        }

        @Override
        public String visit(RoutineNameASTNode n) {
            return null;
        }

        private void prealloc(LiteralASTNode n) {
            if (n instanceof StringLiteralASTNode s) {
                String text = s.getValue();
                String arrayType = "[" + (text.length() + 1) + " x i8]";
                String ptr = "%" + gen.newName();
                addLine(ptr + " = getelementptr "
                        + arrayType + ", "
                        + arrayType + "* @"
                        + symbols.getBinding(n) + ".value, i32 0, i32 0");
                addLine("call void " + STRING_SETTER + "(%struct.Boxed* @" + symbols.getBinding(s) + ", i8* " + ptr + ")");
            }
            else if (n instanceof NumberLiteralASTNode f) {
                String text = Double.toString(f.getValue());
                addLine("call void " + NUMBER_SETTER + "(%struct.Boxed* @" + symbols.getBinding(f) + ", double " + text + ")");
            }
            else if (n instanceof BoolLiteralASTNode b){
                addLine("call void " + BOOL_SETTER + "(%struct.Boxed* @" + symbols.getBinding(b)
                        + ", " + (b.getValue() ? "TRUE" : "FALSE") + ")");
            }
        }

        @Override
        public String visit(RoutineCallASTNode n) {
            addLine("call void @" + n.getFunction() + "()");
            return null;
        }

        @Override
        public String visit(RoutineDeclASTNode n) {
            currentScope = new Scope(n.getName().getText());
            addLine("define void @" + n.getName() + "() {");
            n.getBody().forEach(x -> x.accept(this));
            addLine("ret void\n}");
            currentScope = Scope.TOPLEVEL;
            return null;
        }

        @Override
        public String visit(StringLiteralASTNode n) {
            return "@" + symbols.getBinding(n);
        }

        @Override
        public String visit(WhileLoopASTNode n) {
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
    }
}