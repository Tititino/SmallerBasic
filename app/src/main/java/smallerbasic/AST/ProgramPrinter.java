package smallerbasic.AST;

import org.jetbrains.annotations.NotNull;
import smallerbasic.AST.nodes.*;
import smallerbasic.SymbolTable;

public class ProgramPrinter {

    private final @NotNull SymbolTable<ASTNode> symbols;
    private final @NotNull VarNameGenerator gen;

    private @NotNull StringBuilder llvmProgram = new StringBuilder();

    public ProgramPrinter(@NotNull SymbolTable<ASTNode> symbols,
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
        private final static @NotNull String BOOL_GETTER = "@_GET_BOOL_VALUE";
        private final static @NotNull String NUMBER_SETTER = "@_SET_NUM_VALUE";
        private final static @NotNull String BOOL_SETTER = "@_SET_BOOL_VALUE";
        private final static @NotNull String STRING_SETTER = "@_SET_STR_VALUE";


        @Override
        public String visit(AssStmtASTNode n) {
            String name = n.getVarName().accept(this);
            String rightSide = n.getValue().accept(this);
            llvmProgram.append("call void @COPY(%struct.Boxed* " + name + ", %struct.Boxed* " + rightSide + ")\n");
            return null;
        }

        @Override
        public String visit(BinOpASTNode n) {
            String left = n.getLeft().accept(this);
            String right = n.getRight().accept(this);

            String res = "%" + gen.newName();
            llvmProgram.append(res + " = alloca %struct.Boxed\n");
            llvmProgram.append("call void @" + n.getOp()
                    + "(%struct.Boxed* " + res
                    + ", %struct.Boxed* " + left
                    + ", %struct.Boxed* " + right + ")\n");
            return res;
        }

        @Override
        public String visit(BoolLiteralASTNode n) {
            return "@" + symbols.getBinding(n);
        }

        @Override
        public String visit(ExternalFunctionCallASTNode n) {
            String firstArg = n.getArgs().get(0).accept(this);
            llvmProgram.append("call void @PRINT(%struct.Boxed* " + firstArg + ")\n");
            return null;
        }

        @Override
        public String visit(ForLoopASTNode n) {
            String label = gen.newName();
            (new AssStmtASTNode(n.getVarName(), n.getStart())).accept(this);
            llvmProgram.append("br label %" + label + ".begin\n");
            llvmProgram.append(label + ".begin:\n");

            String cond = (new BinOpASTNode(BinOpASTNode.BinOp.LT, n.getVarName(), n.getEnd())).accept(this);
            String bool = "%" + gen.newName();
            llvmProgram.append(bool + " = call i1 " + BOOL_GETTER + "(%struct.Boxed* " + cond + ")\n");
            llvmProgram.append("br i1 " + bool + ", label %" + label + ".continue, label %" + label + ".end\n");
            llvmProgram.append(label + ".continue:\n");
            n.getBody().forEach(x -> x.accept(this));

            (new AssStmtASTNode(
                    n.getVarName(),
                    new BinOpASTNode(
                            BinOpASTNode.BinOp.PLUS,
                            n.getVarName(),
                            n.getStep()
                    )
            )).accept(this);

            llvmProgram.append("br label %" + label + ".begin\n");
            llvmProgram.append(label + ".end:\n");

            return null;
        }

        @Override
        public String visit(GotoStmtASTNode n) {
            llvmProgram.append("br label %").append(symbols.getBinding(new LabelDeclASTNode(n.getLabel()))).append("\n");
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
            llvmProgram.append(bool + " = call i1 " + BOOL_GETTER + "(%struct.Boxed* " + cond + ")\n");
            llvmProgram.append("br i1 " + bool + ", label %" + label + ".true, label %" + label + ".false\n");
            llvmProgram.append(label + ".true:\n");
            n.getTrueBody().forEach(x -> x.accept(this));
            llvmProgram.append("br label %" + label + ".end\n");
            llvmProgram.append(label + ".false:\n");
            n.getFalseBody().ifPresent(x -> x.forEach(y -> y.accept(this)));
            llvmProgram.append("br label %" + label + ".end\n");
            llvmProgram.append(label + ".end:\n");
            return null;
        }

        @Override
        public String visit(LabelDeclASTNode n) {
            llvmProgram.append(symbols.getBinding(n) + ":\n");
            return null;
        }

        @Override
        public String visit(NumberLiteralASTNode n) {
            return "@" + symbols.getBinding(n);
        }

        @Override
        public String visit(ProgramASTNode n) {
            llvmProgram.append("; variables\n");
            for (ASTNode id : symbols
                    .getSymbols()
                    .stream()
                    .filter(x -> x instanceof IdentifierASTNode)
                    .toList())
                llvmProgram.append("@").append(symbols.getBinding(id)).append(" = global %struct.Boxed { i2 0, i64 0 }\n");

            llvmProgram.append("; boxed constants\n");
            for (ASTNode lit : symbols
                    .getSymbols()
                    .stream()
                    .filter(x -> x instanceof LiteralASTNode)
                    .toList())
                llvmProgram.append("@" + symbols.getBinding(lit)).append(" = global %struct.Boxed { i2 0, i64 0 }\n");

            llvmProgram.append("; string constants\n");
            for (ASTNode lit : symbols
                    .getSymbols()
                    .stream()
                    .filter(x -> x instanceof StringLiteralASTNode)
                    .toList()) {
                String text = ((StringLiteralASTNode) lit).getValue();
                llvmProgram.append("@" + symbols.getBinding(lit)
                        + ".value = constant [" + (text.length() + 1)
                        + " x i8] c\"" + text + "\\00\"\n");
            }

            n.getContents()
                    .stream()
                    .filter(x -> x instanceof RoutineDeclASTNode)
                    .map(x -> (RoutineDeclASTNode) x)
                    .forEach(x -> x.accept(this));

            llvmProgram.append("define void @main() {\n");
            for (ASTNode lit : symbols
                    .getSymbols()
                    .stream()
                    .filter(x -> x instanceof LiteralASTNode)
                    .toList())
                prealloc((LiteralASTNode) lit);

            n.getContents()
                    .stream()
                    .filter(x -> x instanceof StatementASTNode)
                    .map(x -> (StatementASTNode) x)
                    .forEach(x -> x.accept(this));

            llvmProgram.append("ret void\n}\n");
            return null;
        }

        private void prealloc(LiteralASTNode n) {
            if (n instanceof StringLiteralASTNode s) {
                String text = s.getValue();
                String arrayType = "[" + (text.length() + 1) + " x i8]";
                String ptr = "%" + gen.newName();
                llvmProgram.append(ptr + " = getelementptr "
                        + arrayType + ", "
                        + arrayType + "* @"
                        + symbols.getBinding(n) + ".value, i32 0, i32 0\n");
                llvmProgram.append("call void " + STRING_SETTER + "(%struct.Boxed* @" + symbols.getBinding(s) + ", i8* " + ptr + ")\n");
            }
            else if (n instanceof NumberLiteralASTNode f) {
                String text = Double.toString(f.getValue());
                llvmProgram.append("call void " + NUMBER_SETTER + "(%struct.Boxed* @" + symbols.getBinding(f) + ", double " + text + ")\n");
            }
            else if (n instanceof BoolLiteralASTNode b){
                llvmProgram.append("call void " + BOOL_SETTER + "(%struct.Boxed* @" + symbols.getBinding(b)
                        + ", " + (b.getValue() ? "TRUE" : "FALSE") + ")\n");
            }
        }

        // sistemo in modo tale da prendere i nomi dalla symbol table, così da evitare clash
        // faccio lo stesso per i label
        @Override
        public String visit(RoutineCallASTNode n) {
            llvmProgram.append("call void @").append(n.getFunction()).append("()\n");
            return null;
        }

        @Override
        public String visit(RoutineDeclASTNode n) {
            llvmProgram.append("define void @" + n.getName() + "() {\n");
            n.getBody().forEach(x -> x.accept(this));
            llvmProgram.append("ret void\n}\n");
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
            llvmProgram.append("br label %" + label + ".begin\n");  // strange llvm magic
            llvmProgram.append(label + ".begin:\n");
            String cond = n.getCondition().accept(this);
            llvmProgram.append(bool + " = call i1 " + BOOL_GETTER + "(%struct.Boxed* " + cond + ")\n");
            llvmProgram.append("br i1 " + bool + ", label %" + label + ".continue, label %" + label + ".end\n");
            llvmProgram.append(label + ".continue:\n");
            n.getBody().forEach(x -> x.accept(this));
            llvmProgram.append("br label %" + label + ".begin\n");
            llvmProgram.append(label + ".end:\n");
            return null;
        }
    }
}