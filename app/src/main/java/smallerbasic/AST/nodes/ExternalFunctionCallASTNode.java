package smallerbasic.AST.nodes;

import org.jetbrains.annotations.NotNull;
import smallerbasic.AST.ASTVisitor;

import java.util.List;
import java.util.Objects;

public class ExternalFunctionCallASTNode extends AbstractASTNode implements ExprOrStmtASTNode {

    private final @NotNull String module;
    private final @NotNull String function;
    private final @NotNull List<@NotNull ExpressionASTNode> args;

    public ExternalFunctionCallASTNode(
            @NotNull String module,
            @NotNull String function,
            @NotNull List<@NotNull ExpressionASTNode> args
    ) {
        this.module = module;
        this.function = function;
        this.args = args;
    }

    public @NotNull ExpressionASTNode getArg(int i) {
        return args.get(i);
    }

    @Override
    public <T> T accept(ASTVisitor<T> v) {
        return v.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExternalFunctionCallASTNode that = (ExternalFunctionCallASTNode) o;
        return module.equals(that.module) && function.equals(that.function) && args.equals(that.args);
    }

    @Override
    public int hashCode() {
        return Objects.hash(module, function, args);
    }

    @Override
    public String toString() {
        return "ExternalFunctionCallASTNode{" +
                "module='" + module + '\'' +
                ", function='" + function + '\'' +
                ", args=" + args +
                '}';
    }
}
