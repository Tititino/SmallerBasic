package smallerbasic;

import org.antlr.v4.runtime.tree.ParseTree;
import smallerbasic.AST.nodes.ASTNode;
import smallerbasic.AST.staticChecks.DoubleLabelCheck;
import smallerbasic.AST.staticChecks.LabelScopeCheck;
import smallerbasic.AST.staticChecks.Check;
import smallerbasic.compiler.ProgramPrinter;
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
                    new LabelScopeCheck(),
                    new DoubleLabelCheck()
            );
            Optional<ParseTree> parseTree = parse(lex(Paths.get(args[0])));
            if (parseTree.isEmpty()) {
                System.out.println("Compilation failed");
                return;
            }
            Optional<ASTNode> ast = check(clean(parseTree.get()), staticChecks);
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
