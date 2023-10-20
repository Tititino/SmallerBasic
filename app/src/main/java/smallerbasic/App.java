package smallerbasic;

import org.antlr.v4.runtime.*;
import smallerbasic.AST.SymbolTableVisitor;
import smallerbasic.AST.VarNameGenerator;
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


            ast.printLLVM(new VarNameGenerator(), new SymbolTableVisitor(ast));
        } catch (IOException e) {
            System.out.println("Error reading file \"" + args[0] + "\"");
        }
    }
}
