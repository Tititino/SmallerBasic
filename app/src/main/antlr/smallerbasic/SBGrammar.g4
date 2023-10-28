grammar SBGrammar;
@header {
package smallerbasic;
}

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
Number : '+'?(DIGIT+('.'DIGIT*)?|'.'DIGIT+)([eE][-+]?DIGIT+)? ;
String : '"'PRINTABLE*'"' ;
ExternalFunctionName : Ident'.'Ident ;
Ident  : [A-Za-z_][A-Za-z0-9_]* ;
WS     : [ \t\r\n]+ -> skip ;
