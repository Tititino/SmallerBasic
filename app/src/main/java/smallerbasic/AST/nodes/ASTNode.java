package smallerbasic.AST.nodes;

import org.antlr.v4.runtime.ParserRuleContext;

public interface ASTNode {

    static ASTNode fromParseTree(ParserRuleContext parsed) {
        return null;
    }
}
