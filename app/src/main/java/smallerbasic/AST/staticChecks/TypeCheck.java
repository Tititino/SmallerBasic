package smallerbasic.AST.staticChecks;

import org.antlr.v4.runtime.Token;
import org.jetbrains.annotations.NotNull;
import smallerbasic.AST.ASTVisitor;
import smallerbasic.AST.nodes.*;

import java.util.List;

public class TypeCheck extends AbstractCheck {
    private boolean isOk = true;

    @Override
    public boolean check(@NotNull ASTNode n) {
        isOk = true;
        n.accept(new TypingVisitor());
        return isOk;
    }

    @Override
    public void reportError(@NotNull String msg) {
        isOk = false;
        super.reportError(msg);
    }

    public enum TYPE {
        NONE,
        ANY,
        NUMBER,
        STRING,
        BOOL;

        public boolean matches(@NotNull TYPE expected) {
            if (this.equals(ANY))
                return true;
            else if (expected.equals(ANY))
                return true;
            return (this.equals(expected));
        }

        public boolean matches(@NotNull List<TYPE> expected) {
            return expected.stream().anyMatch(this::matches);
        }

        @Override
        public String toString() {
            return switch (this) {
                case NUMBER -> "Number";
                case BOOL -> "Bool";
                case STRING -> "String";
                case ANY -> "Any";
                case NONE -> "Void";
            };
        }
    }

    private void reportMismatch(@NotNull ASTNode n, @NotNull TYPE got, @NotNull TYPE expected) {
        if (!got.matches(expected)) {
            String position = "";
            if (n.getStartToken().isPresent()) {
                Token start = n.getStartToken().get();
                position = " at line " + start.getLine() + ":" + start.getCharPositionInLine();
            }
            reportError("*** TypeError: expected value of type "
                    + expected + " but got "
                    + got + position);
        }
    }

    private void reportMismatch(@NotNull ASTNode n, @NotNull TYPE got, @NotNull List<TYPE> expected) {
        if (!got.matches(expected)) {
            String position = "";
            if (n.getStartToken().isPresent()) {
                Token start = n.getStartToken().get();
                position = " at line " + start.getLine() + ":" + start.getCharPositionInLine();
            }
            reportError("*** TypeError: expected value of type "
                    + expected + " but got "
                    + got + position);
        }
    }

    private class TypingVisitor implements ASTVisitor<TYPE> {

        private TYPE visitChildren(List<? extends ASTNode> l) {
             l.forEach(x -> x.accept(this));
             return TYPE.NONE;
        }

        @Override
        public TYPE visit(AssStmtASTNode n) {
            n.getVarName().accept(this);
            n.getValue().accept(this);
            return TYPE.NONE;
        }

        @Override
        public TYPE visit(BinOpASTNode n) {
            TYPE left = n.getLeft().accept(this);
            TYPE right = n.getRight().accept(this);
            switch (n.getOp()) {
                case AND, OR -> {
                    reportMismatch(n, left, TYPE.BOOL);
                    reportMismatch(n, right, TYPE.BOOL);
                    return TYPE.BOOL;
                }
                case MINUS, DIV, MULT -> {
                    reportMismatch(n, left, TYPE.NUMBER);
                    reportMismatch(n, right, TYPE.NUMBER);
                    return TYPE.NUMBER;
                }
                case LEQ, GEQ, LT, GT -> {
                    reportMismatch(n, left, List.of(TYPE.NUMBER, TYPE.STRING));
                    reportMismatch(n, right, List.of(TYPE.NUMBER, TYPE.STRING));
                    reportMismatch(n, left, right);
                    return TYPE.BOOL;
                }
                case EQ, NEQ -> {
                    reportMismatch(n, left, List.of(TYPE.NUMBER, TYPE.STRING));
                    reportMismatch(n, right, List.of(TYPE.NUMBER, TYPE.STRING));
                    return TYPE.BOOL;
                }
                case PLUS -> {
                    reportMismatch(n, left, List.of(TYPE.NUMBER, TYPE.STRING));
                    reportMismatch(n, right, List.of(TYPE.NUMBER, TYPE.STRING));
                    reportMismatch(n, left, right);
                    if (left.equals(TYPE.STRING))
                        return TYPE.STRING;
                    return TYPE.NUMBER;
                }
            }
            return null;
        }

        @Override
        public TYPE visit(BoolLiteralASTNode n) {
            return TYPE.BOOL;
        }

        @Override
        public TYPE visit(ExternalFunctionCallASTNode n) {
            return TYPE.ANY;
        }

        @Override
        public TYPE visit(ForLoopASTNode n) {
            reportMismatch(n, n.getStart().accept(this), TYPE.NUMBER);
            reportMismatch(n, n.getStart().accept(this), TYPE.NUMBER);
            reportMismatch(n, n.getStep().accept(this), TYPE.NUMBER);
            return visitChildren(n.getBody());
        }

        @Override
        public TYPE visit(GotoStmtASTNode n) {
            return TYPE.NONE;
        }

        @Override
        public TYPE visit(IdentifierASTNode n) {
            return TYPE.ANY;
        }

        @Override
        public TYPE visit(IfThenASTNode n) {
            reportMismatch(n, n.getCondition().accept(this), TYPE.BOOL);
            visitChildren(n.getTrueBody());
            return n.getFalseBody().map(this::visitChildren).orElse(TYPE.NONE);
        }

        @Override
        public TYPE visit(LabelDeclASTNode n) {
            return TYPE.NONE;
        }

        @Override
        public TYPE visit(NumberLiteralASTNode n) {
            return TYPE.NUMBER;
        }

        @Override
        public TYPE visit(ProgramASTNode n) {
            return visitChildren(n.getContents());
        }

        @Override
        public TYPE visit(LabelNameASTNode n) {
            return TYPE.NONE;
        }

        @Override
        public TYPE visit(RoutineNameASTNode n) {
            return TYPE.NONE;
        }

        @Override
        public TYPE visit(RoutineCallASTNode n) {
            return TYPE.NONE;
        }

        @Override
        public TYPE visit(RoutineDeclASTNode n) {
            return visitChildren(n.getBody());
        }

        @Override
        public TYPE visit(StringLiteralASTNode n) {
            return TYPE.STRING;
        }

        @Override
        public TYPE visit(WhileLoopASTNode n) {
            reportMismatch(n, n.getCondition().accept(this), TYPE.BOOL);
            return visitChildren(n.getBody());
        }

        @Override
        public TYPE visit(UnaryMinusASTNode n) {
            reportMismatch(n, n.getExpr().accept(this), TYPE.NUMBER);
            return TYPE.NUMBER;
        }

        @Override
        public TYPE visit(ArrayASTNode n) {
            n.getIndexes().forEach(x -> reportMismatch(x, x.accept(this), TYPE.NUMBER));
            return TYPE.ANY;
        }
    }
}
