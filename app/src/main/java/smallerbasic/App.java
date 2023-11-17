package smallerbasic;

import org.antlr.v4.runtime.TokenStream;
import smallerbasic.AST.nodes.ASTNode;
import smallerbasic.AST.staticChecks.*;
import smallerbasic.AST.staticChecks.errors.PrettyErrorPrinter;
import smallerbasic.compiler.LLVM.LLVMCompiler;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import static smallerbasic.CompilationUtils.*;

public class App {
    private static final List<Check> errors = List.of(
            new MaxNameLengthCheck(),
            new TypeCheck(),
            new LabelScopeCheck(),
            new DoubleLabelCheck(),
            new DoubleRoutineDeclCheck(),
            new RoutineCallCheck()
    );

    private static final List<Check> warnings = List.of(
            new UninitializedVariableCheck()
    );

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("No file provided");
            return;
        }

        try {
            TokenStream tokens = lex(Paths.get(args[0]));
            errors.forEach(x -> x.setErrorReporter(new PrettyErrorPrinter(tokens)));
            warnings.forEach(x -> x.setErrorReporter(new PrettyErrorPrinter(tokens)));

            ASTNode ast = check(
                    clean(
                            parse(tokens).orElseThrow(() -> new CompilationError("Parsing failed"))
                    ),
                    errors,
                    warnings
            ).orElseThrow(() -> new CompilationError("Static checks failed"));

            String program = compile(ast, new LLVMCompiler());

            System.out.println(program);
        } catch (IOException e) {
            System.out.println("Error reading file \"" + args[0] + "\"");
        } catch (CompilationError e) {
            System.out.println(e.getMessage());
        }
    }
}
