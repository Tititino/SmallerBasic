package smallerbasic.AST;

import org.jetbrains.annotations.NotNull;
import smallerbasic.AST.nodes.*;
import smallerbasic.SymbolTable;

public class ProgramPrinter {

    private final @NotNull SymbolTable<IdentifierASTNode> symbols;
    private final @NotNull SymbolTable<String> labels;
    private final @NotNull SymbolTable<String> functions;
    private final @NotNull VarNameGenerator gen;

    private @NotNull StringBuilder llvmProgram = new StringBuilder();

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
        llvmProgram = new StringBuilder();
        n.accept(new ProgramPrinterVisitor());
        return llvmProgram.toString();
    }

    private class ProgramPrinterVisitor implements ASTVisitor<String> {
        private final static @NotNull String BOOL_GETTER = "@GetBoolValue";
        private final static @NotNull String NUMBER_SETTER = "@SetNumberValue";
        private final static @NotNull String BOOL_SETTER = "@SetBoolValue";

        // valuto prima il lato destro di quello sinistro !!!
        @Override
        public String visit(AssStmtASTNode n) {
            symbols.newBinding(n.getVarName());
            String name = symbols.getBinding(n.getVarName());
            String rightSide = n.getValue().accept(this);
            llvmProgram.append("%" + name + " = alloca %struct.Boxed\n");
            llvmProgram.append("call void @COPY(%struct.Boxed* %" + name + ", %struct.Boxed* %" + rightSide + ")\n");
            return name;
        }

        @Override
        public String visit(BinOpASTNode n) {
            String left = n.getLeft().accept(this);
            String right = n.getRight().accept(this);

            String res = gen.newName();
            llvmProgram.append("%" + res + " = alloca %struct.Boxed\n");
            llvmProgram.append("call void @" + n.getOp()
                    + "(%struct.Boxed* %" + res
                    + ", %struct.Boxed* %" + left
                    + ", %struct.Boxed* %" + right + ")\n");
            return res;
        }

        @Override
        public String visit(BoolLiteralASTNode n) {
            String name = gen.newName();
            llvmProgram.append("%" + name + " = alloca %struct.Boxed\n");
            llvmProgram.append("call void " + BOOL_SETTER + "(%struct.Boxed* %"
                    + name + ", " + (n.getValue() ? "TRUE" : "FALSE") + ")\n");
            return name;
        }

        @Override
        public String visit(ExternalFunctionCallASTNode n) {
            String firstArg = n.getArg(0).accept(this);
            llvmProgram.append("call void @PRINT(%struct.Boxed* %" + firstArg + ")\n");
            return null;
        }

        @Override
        public String visit(ForLoopASTNode n) {
            String start = n.getStart().accept(this);
            String end   = n.getEnd().accept(this);

            symbols.newBinding(n.getVarName());
            String name = symbols.getBinding(n.getVarName());
            llvmProgram.append("%" + name + " = alloca %struct.Boxed\n");
            llvmProgram.append("call void @COPY(%struct.Boxed* %" + name + ", %struct.Boxed* %" + start + ")\n");


            String flooredStart = gen.newName();
            llvmProgram.append("%" + flooredStart + " = alloca i64\n");
            String floorResult = gen.newName();
            llvmProgram.append("%" + floorResult + " = call i64 @Floor(%struct.Boxed* %" + start + ")\n");
            llvmProgram.append("store i64 %" + floorResult + ", i64* %" + floorResult + "\n");

            String flooredEnd = gen.newName();
            llvmProgram.append("%" + flooredEnd + " = call i64 @Floor(%struct.Boxed* %" + end + ")\n");

            String testLabel = gen.newName();
            String bodyLabel  = gen.newName();
            String endLabel  = gen.newName();
            llvmProgram.append(testLabel + ":\n");
            String temp = gen.newName();
            llvmProgram.append("%" + temp + " = load i64, i64* %" + flooredStart);
            String bool = gen.newName();
            llvmProgram.append("%" + bool + " = icmp eq i64 %" + temp + ", %" + flooredEnd + "\n");
            llvmProgram.append("br i1 %" + bool + "label %" + bodyLabel + ", label %" + endLabel + "\n");
            llvmProgram.append(bodyLabel + ":\n");

            n.getBody().forEach(x -> x.accept(this));

            // manca aggiungere + 1 all variablie
            // manca gestione degli step
            String temp2 = gen.newName();
            llvmProgram.append("%" + temp2 + " = add i64 %" + temp + ", 1\n");
            llvmProgram.append("store i64 %" + temp2 + ", i64* %" + floorResult + "\n");
            llvmProgram.append("br label %" + testLabel + "\n");
            llvmProgram.append(endLabel + ":\n");
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
            llvmProgram.append("ret void\n}\n");
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