package smallerbasic.AST;

import org.jetbrains.annotations.NotNull;
import smallerbasic.AST.nodes.*;
import smallerbasic.SymbolTable;

public class ProgramPrinter {

    private final @NotNull SymbolTable<IdentifierASTNode> symbols;

    private final @NotNull SymbolTable<String> labels;
    private final @NotNull SymbolTable<String> functions;
    private final @NotNull VarNameGenerator gen;

    private final @NotNull StringBuilder llvmProgram = new StringBuilder();

    public ProgramPrinter(@NotNull SymbolTable<IdentifierASTNode> symbols,
                          @NotNull SymbolTable<String> labels,
                          @NotNull SymbolTable<String> functions,
                          @NotNull VarNameGenerator gen) {
        this.symbols = symbols;
        this.labels = labels;
        this.functions = functions;
        this.gen = gen;
    }

    public @NotNull String compile(@NotNull ASTNode n) {
        return n.accept(new ProgramPrinterVisitor()).toString();
    }

    private class ProgramPrinterVisitor implements ASTVisitor<String> {
        private final static @NotNull String BOOL_GETTER = "@GetBoolValue";
        private final static @NotNull String NUMBER_SETTER = "@SetNumberValue";
        private final static @NotNull String BOOL_SETTER = "@SetBoolValue";

        @Override
        public String visit(AssStmtASTNode n) {
            String name = gen.newName();
            String rightSide = n.accept(this);
            llvmProgram.append("%" + name + " = alloca %struct.Boxed\n");
            llvmProgram.append("call void @COPY(%struct.Boxed* %" + name + ", %struct.Boxed* %" + rightSide + ")\n");
            return name;
        }

        @Override
        public String visit(BinOpASTNode n) {
            String left = n.getLeft().accept(this);
            String right = n.getRight().accept(this);

            String res = gen.newName();
            llvmProgram.append("%" + res + " = alloca %struct.Boxed");
            llvmProgram.append("call void @" + n.getOp()
                    + "(%struct.Boxed* %" + res
                    + ", %struct.Boxed* %" + left
                    + ", %struct.Boxed* %" + right + ")\n");
            return res;
        }

        @Override
        public String visit(BoolLiteralASTNode n) {
            String name = gen.newName();
            llvmProgram.append("%" + name + " = alloca %struct.Boxed*\n");
            llvmProgram.append("call void " + BOOL_SETTER + "(%struct.Boxed* %"
                    + name + ", " + (n.getValue() ? "TRUE" : "FALSE") + ")\n");
            return name;
        }

        @Override
        public String visit(ExternalFunctionCallASTNode n) {
            return null;
        }

        @Override
        public String visit(ForLoopASTNode n) {
            return null;
        }

        @Override
        public String visit(GotoStmtASTNode n) {
            llvmProgram.append("br label %").append(n.getLabel()).append("\n");
            return null;
        }

        @Override
        public String visit(IdentifierASTNode n) {
            return symbols.getBinding(n);
        }

        @Override
        public String visit(IfThenASTNode n) {
            String cond = n.getCondition().accept(this);
            String bool = gen.newName();
            String trueLabel = gen.newName();
            String falseLabel = gen.newName();
            String endLabel = gen.newName();
            llvmProgram.append("%" + bool + " = call i1 " + BOOL_GETTER + "(%struct.Boxed* %" + cond + ")\n");
            llvmProgram.append("br i1 %" + bool + ", label %" + trueLabel + ", label %" + falseLabel + "\n");
            llvmProgram.append(trueLabel + ":\n");
            n.getTrueBody().forEach(x -> x.accept(this));
            llvmProgram.append("br label %" + endLabel + "\n");
            llvmProgram.append(trueLabel + ":\n");
            n.getFalseBody().ifPresent(x -> x.forEach(y -> y.accept(this)));
            llvmProgram.append("br label %" + endLabel + "\n");
            return null;
        }

        @Override
        public String visit(LabelDeclASTNode n) {
            llvmProgram.append(labels.getBinding(n.getName()) + ":\n");
            return null;
        }

        @Override
        public String visit(NumberLiteralASTNode n) {
            String name = gen.newName();
            llvmProgram.append("%" + name + " = alloca %struct.Boxed*\n");
            llvmProgram.append("call void " + NUMBER_SETTER + "(%struct.Boxed* %" + name + ", double " + n.getValue() + ")\n");
            return name;
        }

        @Override
        public String visit(ProgramASTNode n) {
            for (IdentifierASTNode id : symbols)
                llvmProgram.append("%").append(symbols.getBinding(id)).append(" = alloca %struct.Boxed\n");

            n.getContents()
                    .stream()
                    .filter(x -> x instanceof RoutineDeclASTNode)
                    .map(x -> (RoutineDeclASTNode) x)
                    .forEach(x -> x.accept(this));

            llvmProgram.append("define @main() {\n");
            n.getContents()
                    .stream()
                    .filter(x -> x instanceof StatementASTNode)
                    .map(x -> (StatementASTNode) x)
                    .forEach(x -> x.accept(this));

            llvmProgram.append("ret void\n}\n");
            return null;
        }

        @Override
        public String visit(RoutineCallASTNode n) {
            llvmProgram.append("call void @" + n.getFunction() + "()\n");
            return null;
        }

        @Override
        public String visit(RoutineDeclASTNode n) {
            llvmProgram.append("define void @" + functions.getBinding(n.getName()) + "() {\n");
            n.getBody().forEach(x -> x.accept(this));
            llvmProgram.append("}\n");
            return null;
        }

        @Override
        public String visit(StringLiteralASTNode n) {
            return null;
        }

        @Override
        public String visit(WhileLoopASTNode n) {
            return null;
        }
    }
}