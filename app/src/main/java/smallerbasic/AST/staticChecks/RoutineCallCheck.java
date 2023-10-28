package smallerbasic.AST.staticChecks;

import org.antlr.v4.runtime.Token;
import org.jetbrains.annotations.NotNull;
import smallerbasic.AST.ASTMonoidVisitor;
import smallerbasic.AST.nodes.*;

import java.util.HashSet;

public class RoutineCallCheck extends AbstractCheck {


    public boolean check(@NotNull ASTNode n) {
        boolean isOk = true;
        HashSet<RoutineNameASTNode> declared = new HashSet<>();
        HashSet<RoutineNameASTNode> called = new HashSet<>();
        n.accept(new CallsVisitor(declared, called));
        for (RoutineNameASTNode l : called)
            if (!declared.contains(l)) {
                isOk = false;
                reportError(l, String.format(
                                "*** RoutineCallError: routine \"%s\" called at line %d is not defined",
                                l.getText(),
                                l.getStartToken().map(Token::getLine).orElse(-1)
                        )
                );
            }
        return isOk;
    }

    private record CallsVisitor(@NotNull HashSet<RoutineNameASTNode> declared,
                                @NotNull HashSet<RoutineNameASTNode> called) implements ASTMonoidVisitor<Void> {

            private CallsVisitor(@NotNull HashSet<RoutineNameASTNode> declared, HashSet<RoutineNameASTNode> called) {
                this.declared = declared;
                this.called = called;
            }

            @Override
            public Void empty() {
                return null;
            }

            @Override
            public Void compose(Void o1, Void o2) {
                return null;
            }

            @Override
            public Void visit(RoutineCallASTNode n) {
                called.add(n.getFunction());
                return null;
            }

            @Override
            public Void visit(RoutineDeclASTNode n) {
                declared.add(n.getName());
                return null;
            }
        }
}
