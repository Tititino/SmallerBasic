package smallerbasic;

import org.antlr.v4.runtime.*;
import smallerbasic.AST.nodes.ASTNode;

import java.io.IOException;

public class App {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("No file provided");
            return;
        }

        try {
            TokenStream lexedSource = CompilationUtils.lex(args[0]);
            ParserRuleContext parsedSource = CompilationUtils.parse(lexedSource);
            ASTNode ast = CompilationUtils.clean(parsedSource);

            System.out.println(ast);
        } catch (IOException e) {
            System.out.println("Error reading file \"" + args[0] + "\"");
        }
    }
}
