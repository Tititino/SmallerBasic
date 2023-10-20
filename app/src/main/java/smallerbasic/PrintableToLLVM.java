package smallerbasic;

import smallerbasic.AST.SymbolTableVisitor;
import smallerbasic.AST.VarNameGenerator;

public interface PrintableToLLVM {
    void printLLVM(VarNameGenerator gen, SymbolTableVisitor symbols);
}
