package smallerbasic.AST;

import smallerbasic.AST.nodes.*;

import java.util.List;

/**
 * This class, given an identity and an associative function to compose results, provides a
 * default implementation of {@link ASTVisitor} that traverses the AST composing the results.
 */
public interface ASTMonoidVisitor<T> extends ASTVisitor<T> {

     default T visitChildren(List<? extends ASTNode> l) {
        return l.stream()
                .map(x -> x.accept(this))
                .reduce(empty(), this::compose);
    }
    T empty();
    T compose(T o1, T o2);

    @Override
    default T visit(AssStmtASTNode n) {
        return compose(n.getVarName().accept(this), n.getValue().accept(this));
    }

    @Override
    default T visit(BinOpASTNode n) {
        return compose(n.getLeft().accept(this), n.getRight().accept(this));
    }

    @Override
    default T visit(BoolLiteralASTNode n) {
        return empty();
    }

    @Override
    default T visit(ExternalFunctionCallASTNode n) {
        return n.getArgs()
                .stream()
                .map(x -> x.accept(this))
                .reduce(empty(), this::compose);
    }

    @Override
    default T visit(ForLoopASTNode n) {
        return compose(
                n.getVarName().accept(this),
                compose(
                        n.getStart().accept(this),
                        compose(
                                n.getEnd().accept(this),
                                compose(
                                        n.getStep().accept(this),
                                        visitChildren(n.getBody())
                                )
                        )
                )
        );
    }

    @Override
    default T visit(GotoStmtASTNode n) {
        return visit(n.getLabel());
    }

    @Override
    default T visit(IdentifierASTNode n) {
        return empty();
    }

    @Override
    default T visit(IfThenASTNode n) {
        return compose(
                n.getCondition().accept(this),
                compose(
                        visitChildren(n.getTrueBody()),
                        n.getFalseBody().map(this::visitChildren).orElse(empty())
                )
        );
    }

    @Override
    default T visit(LabelDeclASTNode n) {
        return visit(n.getName());
    }

    @Override
    default T visit(NumberLiteralASTNode n) {
        return empty();
    }

    @Override
    default T visit(ProgramASTNode n) {
        return visitChildren(n.getContents());
    }

    @Override
    default T visit(RoutineCallASTNode n) {
        return visit(n.getFunction());
    }

    @Override
    default T visit(RoutineDeclASTNode n) {
        return compose(visit(n.getName()), visitChildren(n.getBody()));
    }

    @Override
    default T visit(RoutineNameASTNode n) {
        return empty();
    }
    @Override
    default T visit(LabelNameASTNode n) {
        return empty();
    }

    @Override
    default T visit(StringLiteralASTNode n) {
        return empty();
    }

    @Override
    default T visit(WhileLoopASTNode n) {
        return compose(
                n.getCondition().accept(this),
                visitChildren(n.getBody())
        );
    }

    @Override
    default T visit(UnaryMinusASTNode n) {
        return n.getExpr().accept(this);
    }

    default T visit(ArrayASTNode n) {
        return n.getIndexes()
                .stream()
                .map(x -> x.accept(this))
                .reduce(visit(n.getName()), this::compose);
    }

}
