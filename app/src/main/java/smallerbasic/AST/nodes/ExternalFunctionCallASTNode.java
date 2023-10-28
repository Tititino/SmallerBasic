package smallerbasic.AST.nodes;

import org.jetbrains.annotations.NotNull;
import smallerbasic.AST.ASTVisitor;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * An {@link ASTNode} representing a call to an external function.
 * An external function may be used as a statement (e.g. {@code IO.WriteLine("ciao")}) or as an expression
 * (e.g. {@code X = IO.ReadLine()}).
 */
public class ExternalFunctionCallASTNode extends AbstractASTNode implements StatementASTNode, ExpressionASTNode {

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

    public @NotNull String getModule() {
        return module;
    }

    public @NotNull String getFunction() {
        return function;
    }

    public @NotNull List<ExpressionASTNode> getArgs() {
        return Collections.unmodifiableList(args);
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
