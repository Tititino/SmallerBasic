package smallerbasic.AST.staticChecks;

import org.antlr.v4.runtime.Token;
import org.jetbrains.annotations.NotNull;
import smallerbasic.AST.ASTVisitor;
import smallerbasic.AST.nodes.*;

import java.util.List;

/**
 * This check verifies whether a program is well-typed.
 * Variables and arrays are assigned a type of {@code ANY} and no effort is made to try to
 * guess the type of a variable through assignments.
 */
public class TypeCheck extends AbstractCheck {
    private boolean isOk = true;

    @Override
    public boolean check(@NotNull ASTNode n) {
        isOk = true;
        n.accept(new TypingVisitor());
        return isOk;
    }

    @Override
    public void reportError(@NotNull ASTNode n, @NotNull String msg) {
        isOk = false;
        super.reportError(n, msg);
    }

    /**
     * Something in a Smaller Basic program may have one of three types: NUMBER, BOOL or STRING.
     * A statement has type NONE, and a variable has type ANY.
     */
    public enum TYPE {
        NONE,
        ANY,
        NUMBER,
        STRING,
        BOOL;

        /**
         * Returns whether a {@link TYPE} matches another.
         */
        public boolean matches(@NotNull TYPE expected) {
            if (this.equals(ANY))
                return true;
            else if (expected.equals(ANY))
                return true;
            return (this.equals(expected));
        }

        /**
         * Returns whether a {@link TYPE} matches any in the expected list.
         */
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

    /**
     * Report an error mismatch if {@code got} is different {@code expected}.
     * The {@link ASTNode} is needed for positional information.
     */
    private void reportMismatch(@NotNull ASTNode n, @NotNull TYPE got, @NotNull TYPE expected) {
        if (!got.matches(expected)) {
            String position = "";
            if (n.getStartToken().isPresent() && n.getEndToken().isPresent()) {
                Token start = n.getStartToken().get();
                Token end   = n.getEndToken().get();
                position = " at line " + start.getLine() + ":" + start.getCharPositionInLine()
                        + "-" + (end.getCharPositionInLine() + end.getText().length());
            }
            reportError(n, "*** TypeError: expected value of type "
                    + expected + " but got "
                    + got + position);
        }
    }

    /**
     * Report an error mismatch if {@code got} is not in the {@code expected} list.
     * The {@link ASTNode} is needed for positional information.
     */
    private void reportMismatch(@NotNull ASTNode n, @NotNull TYPE got, @NotNull List<TYPE> expected) {
        if (!got.matches(expected)) {
            String position = "";
            if (n.getStartToken().isPresent() && n.getEndToken().isPresent()) {
                Token start = n.getStartToken().get();
                Token end = n.getEndToken().get();
                position = " at line " + start.getLine() + ":" + start.getCharPositionInLine()
                + "-" + (end.getCharPositionInLine() + end.getText().length());
            }
            reportError(n, "*** TypeError: expected value of type "
                    + expected + " but got "
                    + got + position);
        }
    }

    /**
     * A {@link ASTVisitor} to assign types to each {@link ASTNode}.
     */
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
                    reportMismatch(n.getLeft(), left, TYPE.BOOL);
                    reportMismatch(n.getRight(), right, TYPE.BOOL);
                    return TYPE.BOOL;
                }
                case MINUS, DIV, MULT -> {
                    reportMismatch(n.getLeft(), left, TYPE.NUMBER);
                    reportMismatch(n.getRight(), right, TYPE.NUMBER);
                    return TYPE.NUMBER;
                }
                case LEQ, GEQ, LT, GT -> {
                    reportMismatch(n.getLeft(), left, List.of(TYPE.NUMBER, TYPE.STRING));
                    reportMismatch(n.getRight(), right, List.of(TYPE.NUMBER, TYPE.STRING));
                    reportMismatch(n.getLeft(), left, right);
                    return TYPE.BOOL;
                }
                case EQ, NEQ -> {
                    reportMismatch(n.getLeft(), left, List.of(TYPE.NUMBER, TYPE.STRING));
                    reportMismatch(n.getRight(), right, List.of(TYPE.NUMBER, TYPE.STRING));
                    return TYPE.BOOL;
                }
                case PLUS -> {
                    reportMismatch(n.getLeft(), left, List.of(TYPE.NUMBER, TYPE.STRING));
                    reportMismatch(n.getRight(), right, List.of(TYPE.NUMBER, TYPE.STRING));
                    reportMismatch(n.getLeft(), left, right);
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
            visitChildren(n.getArgs());
            return TYPE.ANY;
        }

        @Override
        public TYPE visit(ForLoopASTNode n) {
            reportMismatch(n.getStart(), n.getStart().accept(this), TYPE.NUMBER);
            reportMismatch(n.getEnd(), n.getStart().accept(this), TYPE.NUMBER);
            reportMismatch(n.getStep(), n.getStep().accept(this), TYPE.NUMBER);
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
            reportMismatch(n.getCondition(), n.getCondition().accept(this), TYPE.BOOL);
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
            reportMismatch(n.getCondition(), n.getCondition().accept(this), TYPE.BOOL);
            return visitChildren(n.getBody());
        }

        @Override
        public TYPE visit(UnaryMinusASTNode n) {
            reportMismatch(n.getExpr(), n.getExpr().accept(this), TYPE.NUMBER);
            return TYPE.NUMBER;
        }

        @Override
        public TYPE visit(ArrayASTNode n) {
            n.getIndexes().forEach(x -> reportMismatch(x, x.accept(this), TYPE.NUMBER));
            return TYPE.ANY;
        }
    }
}
