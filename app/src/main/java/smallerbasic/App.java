package smallerbasic;

import smallerbasic.AST.nodes.ASTNode;
import smallerbasic.AST.staticChecks.*;
import smallerbasic.compiler.ProgramPrinter;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import static smallerbasic.CompilationUtils.*;

public class App {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("No file provided");
            return;
        }

        try {
            List<Check> errors = List.of(
                    new TypeCheck(),
                    new LabelScopeCheck(),
                    new DoubleLabelCheck(),
                    new DoubleRoutineDeclCheck(),
                    new RoutineCallCheck()
            );
            List<Check> warnings = List.of(
                    new UninitializedVariableCheck()
            );
            ASTNode ast = check(
                    clean(
                            parse(lex(Paths.get(args[0]))).orElseThrow(() -> new RuntimeException("Compilation failed"))
                    ),
                    errors,
                    warnings
            ).orElseThrow(() -> new RuntimeException("Static checks failed"));

            String program = ProgramPrinter.compile(ast);

            System.out.println(program);
        } catch (IOException e) {
            System.out.println("Error reading file \"" + args[0] + "\"");
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
        }
    }
}
