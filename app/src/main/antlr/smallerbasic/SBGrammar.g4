grammar SBGrammar;
@header {
package smallerbasic;
}

/**
 * A program is a list of statemetns or routine declarations.
 * Routine declarations are not treated as statements to prevent nested definitions since the body of
 * a routine is also a list of statements.
 */
program : (statement | subroutineDecl)* EOF
        ;

assignmentStmt : var=variable Equal expr=expression
               ;

variable : name=varName                              # Var
         | name=varName ('[' expr+=expression ']')+  # Array
         ;

label : labelName ':' ;

varName : Ident ;

labelName : Ident ;

functionName : Ident ;

statement : assignmentStmt
          | ifStmt
          | forStmt
          | whileStmt
          | gotoStmt
          | callRoutine
          | callExternalFunction
          | label
          ;

callRoutine : functionName '(' ')' ;

callExternalFunction : name=ExternalFunctionName '(' args+=expression? (',' args+=expression)* ')' ;

subroutineDecl : 'Sub' name=functionName
                     body+=statement*
                 'EndSub' ;

/**
 * No type coherence is enforced at parse type.
 * Types are checked after the transfomation to an AST by the {@link TypeCheck} static check.
 * Previosly the grammar was built to try to parse only well-formed expressions (e.g. not '10 < "ciao"').
 * This was abandoned if favour of a simpler grammar, making the conversion to the AST
 * also less tedious and the source more readable.
 */
expression : left=expression op=('*'|'/') right=expression      # MulDivExpr
           | left=expression op=('+'|'-') right=expression      # PlusMinExpr
           | left=expression op=(Relop|Equal) right=expression  # RelopExpr
           | left=expression op=Boolop right=expression         # BoolopExpr
           | '-' val=atom                                       # UnaryMinusExpr
           | atom                                               # AtomExpr
           ;

atom : String                                                   # StringLit
     | Number                                                   # NumberLit
     | Bool                                                     # BoolLit
     | variable                                                 # VarExpr
     | callExternalFunction                                     # Extern
     | '(' body=expression ')'                                  # Parens
     ;

ifStmt : 'If' '(' cond=expression ')' 'Then'
             bodyTrue+=statement*
          ('Else'
             bodyFalse+=statement*
          )?
          'EndIf'
       ;

forStmt : 'For' var=variable Equal from=expression 'To' to=expression ('Step' step=expression)?
              body+=statement*
          'EndFor'
        ;

whileStmt : 'While' '(' cond=expression ')'
                body+=statement*
            'EndWhile'
          ;

gotoStmt  : 'Goto' lbl=labelName
          ;

fragment DIGIT : [0-9] ;
fragment PRINTABLE : ~["] ;

Bool   : 'true' | 'false' ;
Equal  : '=' ;
Relop  : ('<='|'<>'|'<'|'>'|'>=') ;
Boolop : ('And'|'Or') ;
// The number token regex does not accept a preceding minus, since unary minus is already accounted for by
// the expression rule.
// Permitting it in the regex would make something like '--1' legal.
Number : '+'?(DIGIT+('.'DIGIT*)?|'.'DIGIT+)([eE][-+]?DIGIT+)? ;
String : '"'PRINTABLE*'"' ;
ExternalFunctionName : Ident'.'Ident ;
Ident  : [A-Za-z_][A-Za-z0-9_]* ;
WS     : [ \t\r\n]+ -> skip ;
