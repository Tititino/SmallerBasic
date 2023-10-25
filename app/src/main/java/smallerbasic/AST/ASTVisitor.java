package smallerbasic.AST;

import smallerbasic.AST.nodes.*;

public interface ASTVisitor<T> {

    T visit(AssStmtASTNode n);
    T visit(BinOpASTNode n);
    T visit(BoolLiteralASTNode n);
    T visit(ExternalFunctionCallASTNode n);
    T visit(ForLoopASTNode n);
    T visit(GotoStmtASTNode n);
    T visit(IdentifierASTNode n);
    T visit(IfThenASTNode n);
    T visit(LabelDeclASTNode n);
    T visit(NumberLiteralASTNode n);
    T visit(ProgramASTNode n);
    T visit(LabelNameASTNode n);
    T visit(RoutineNameASTNode n);
    T visit(RoutineCallASTNode n);
    T visit(RoutineDeclASTNode n);
    T visit(StringLiteralASTNode n);
    T visit(WhileLoopASTNode n);

    T visit(UnaryMinusASTNode n);

    T visit(ArrayASTNode n);

}
