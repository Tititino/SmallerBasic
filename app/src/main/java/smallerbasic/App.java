package smallerbasic;

import smallerbasic.AST.*;
import smallerbasic.AST.nodes.ASTNode;

import java.io.IOException;
import java.nio.file.Paths;

import static smallerbasic.CompilationUtils.*;

public class App {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("No file provided");
            return;
        }

        try {
            ASTNode ast = clean(parse(lex(Paths.get(args[0]))));

            System.out.println(ast);
            VarNameGenerator gen = new VarNameGenerator();
            String program = new ProgramPrinter(new VariableNames(ast, gen),
                    new LabelNames(ast, gen),
                    new FunctionNames(ast, gen),
                    gen
            ).compile(ast);

            System.out.println(program);
        } catch (IOException e) {
            System.out.println("Error reading file \"" + args[0] + "\"");
        }
    }
}
