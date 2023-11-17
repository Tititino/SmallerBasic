package smallerbasic.AST.staticChecks;

import org.jetbrains.annotations.NotNull;
import smallerbasic.AST.ASTVisitor;
import smallerbasic.AST.nodes.*;

import java.util.List;

/**
 * This check verifies whether a program is well-typed.
 * Variables and arrays are assigned a type of {@link TYPE#ANY} and no effort is made to try and guess the type of a variable through assignments.
 */
public class TypeCheck extends AbstractCheck {
    private boolean isOk = true;

    @Override
    public boolean check(@NotNull ASTNode n) {
        isOk = true;
        n.accept(new TypingVisitor());
        return isOk;
    }

    private void reportError(@NotNull ASTNode n, @NotNull String msg) {
        isOk = false;
        super.reporter.reportError(n, msg);
    }

    /**
     * Something in a SmallerBasic program may have one of three types: {@link TYPE#NUMBER}, {@link TYPE#BOOL} or {@link TYPE#STRING}.
     * A statement has type {@link TYPE#NONE}, and a variable has type {@link TYPE#ANY}.
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
     * The {@link ASTNode} is needed to report the error if one is encountered.
     * @return {@code true} if a mismatch has been reported.
     */
    private boolean reportMismatch(@NotNull ASTNode n, @NotNull TYPE got, @NotNull TYPE expected) {
        boolean matches = got.matches(expected);
        if (!matches) {
            reportError(n, "*** TypeError: expected value of type "
                    + expected + " but got "
                    + got);
        }
        return !matches;
    }

    /**
     * Report an error mismatch if {@code got} is not in the {@code expected} list.
     * The {@link ASTNode} is needed to report the error if one is encountered.
     * @return {@code true} if a mismatch has been reported.
     */
    private boolean reportMismatch(@NotNull ASTNode n, @NotNull TYPE got, @NotNull List<TYPE> expected) {
        boolean matches = got.matches(expected);
        if (!matches) {
            reportError(n, "*** TypeError: expected value of type "
                    + expected + " but got "
                    + got);
        }
        return !matches;
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
                    boolean leftMatch = reportMismatch(n.getLeft(), left, List.of(TYPE.NUMBER, TYPE.STRING));
                    boolean rightMatch = reportMismatch(n.getRight(), right, List.of(TYPE.NUMBER, TYPE.STRING));
                    if (!(leftMatch && rightMatch))
                        return TYPE.ANY;
                    reportMismatch(n.getLeft(), left, right);
                    return TYPE.BOOL;
                }
                case EQ, NEQ -> {
                    reportMismatch(n.getLeft(), left, List.of(TYPE.NUMBER, TYPE.STRING));
                    reportMismatch(n.getRight(), right, List.of(TYPE.NUMBER, TYPE.STRING));
                    return TYPE.BOOL;
                }
                case PLUS -> {
                    boolean leftMatch = reportMismatch(n.getLeft(), left, List.of(TYPE.NUMBER, TYPE.STRING));
                    boolean rightMatch = reportMismatch(n.getRight(), right, List.of(TYPE.NUMBER, TYPE.STRING));
                    if (!(leftMatch && rightMatch))
                        return TYPE.ANY;
                    reportMismatch(n.getLeft(), left, right);
                    return left;
                }
            }
            return TYPE.ANY;
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
