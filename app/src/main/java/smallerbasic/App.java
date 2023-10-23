package smallerbasic;

import smallerbasic.AST.nodes.ASTNode;
import smallerbasic.AST.staticChecks.DoubleLabelCheck;
import smallerbasic.AST.staticChecks.ASTLabelScopeChecking;
import smallerbasic.AST.staticChecks.Check;
import smallerbasic.symbolTable.SymbolTable;
import smallerbasic.symbolTable.VarNameGenerator;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import static smallerbasic.CompilationUtils.*;

public class App {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("No file provided");
            return;
        }

        try {
            List<Check> staticChecks = List.of(
                    new ASTLabelScopeChecking(),
                    new DoubleLabelCheck()
            );
            Optional<ASTNode> ast = check(clean(parse(lex(Paths.get(args[0])))), staticChecks);
            if (ast.isEmpty()) {
                System.out.println("Static checks failed");
                return;
            }
            VarNameGenerator gen = new VarNameGenerator();
            String program = new ProgramPrinter(new SymbolTable(ast.get(), gen), gen).compile(ast.get());

            System.out.println(program);
        } catch (IOException e) {
            System.out.println("Error reading file \"" + args[0] + "\"");
        }
    }
}
