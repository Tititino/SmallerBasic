package smallerbasic;

import org.antlr.v4.runtime.TokenStream;
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
            System.err.println("No file provided");
            System.exit(1);
        }
        try {
            TokenStream tokens = lex(Paths.get(args[0]));
            errors.forEach(x -> x.setErrorReporter(new PrettyErrorPrinter(tokens)));
            warnings.forEach(x -> x.setErrorReporter(new PrettyErrorPrinter(tokens)));
            System.out.println(
                    compile(check(clean(parse(tokens)), errors, warnings), new LLVMCompiler())
            );
        } catch (IOException e) {
            System.err.println("Error reading file \"" + args[0] + "\"");
            System.exit(1);
        } catch (CompilationError e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }
}
