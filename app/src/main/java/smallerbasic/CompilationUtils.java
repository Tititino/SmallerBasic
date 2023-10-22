package smallerbasic;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.jetbrains.annotations.NotNull;
import smallerbasic.AST.ParseTreeToASTVisitor;
import smallerbasic.AST.nodes.ASTNode;
import smallerbasic.AST.staticChecks.Check;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class CompilationUtils {
    public static @NotNull TokenStream lex(@NotNull Path path) throws IOException {
        return new CommonTokenStream(new SBGrammarLexer(CharStreams.fromFileName(path.toString())));
    }

    public static @NotNull TokenStream lex(@NotNull String text) {
        return new CommonTokenStream(new SBGrammarLexer(CharStreams.fromString(text)));
    }

    public static @NotNull ParseTree parse(@NotNull TokenStream tokens) {
        return (new SBGrammarParser(tokens)).program();
    }

    public static @NotNull ASTNode clean(@NotNull ParseTree tree) {
        return (new ParseTreeToASTVisitor()).visit(tree);
    }

    public static @NotNull ASTNode check(@NotNull ASTNode tree, @NotNull List<Check> checks) {
        checks.forEach(x -> x.check(tree));
        return tree;
    }
}
