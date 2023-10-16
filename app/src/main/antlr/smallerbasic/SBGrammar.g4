grammar SBGrammar;
@header {
package smallerbasic;
}

program : (statement* | subroutineDecl*)
        ;

assignmentStmt : Ident '=' expression
               | Ident '=' Ident
               ;

label : Ident ':';

statement : assignmentStmt  NL
          | ifStmt          NL
          | forStmt         NL
          | whileStmt       NL
          | gotoStmt        NL
          | callRoutine     NL
          | label           NL
          ;

callRoutine : FunctionCall ')'
            | ExternalFunctionCall args+=expression? (',' args+=expression)* ')'
            ;

subroutineDecl : 'Sub' name=Ident NL body+=statement* 'EndSub' NL ;

expression : arithExpression
           | stringExpression
           | booleanExpression
           ;

ifStmt : 'If' '(' cond=booleanExpression ')' 'Then' NL
            bodyTrue+=statement*
          ('Else' NL
            bodyFalse+=statement*
          )?
          'EndIf'
       ;

forStmt : 'For' var=Ident '=' from=arithExpression 'To' to=arithExpression ('Step' step=arithExpression)? NL
            body+=statement*
          'EndFor'
        ;

whileStmt : 'While' '(' cond=booleanExpression ')' NL
                body+=statement*
            'EndWhile'
          ;

gotoStmt  : 'Goto' lbl=Ident
          ;

booleanExpression : left=arithExpression relop=('<='|'='|'<>'|'<'|'>'|'>=') right=arithExpression       # NumberComparison
                  | left=stringExpression relop=('<='|'='|'<>'|'<'|'>'|'>=') right=stringExpression     # StringComparison
                  | left=booleanExpression binop=('And'|'Or') right=booleanExpression                   # BoolOp
                  | '(' expr=booleanExpression ')'                                                      # BParens
                  | Bool                                                                                # BoolLiteral
                  | Ident                                                                               # BoolIdent
                  ;

stringExpression : left=stringExpression '+' right=stringExpression                         # StringConcat
                 | '(' expr=stringExpression ')'                                            # SParens
                 | String                                                                   # StringLiteral
                 | Ident                                                                    # StringIdent
                 ;

arithExpression : left=arithExpression op=('/' | '*') right=arithExpression                 # DivMul
                | left=arithExpression op=('+' | '-') right=arithExpression                 # PlusMin
                | '(' expr=arithExpression ')'                                              # NParens
                | Number                                                                    # NumberLiteral
                | Ident                                                                     # NumberIdent
                ;

fragment DIGIT : [0-9] ;
fragment PRINTABLE : ~["] ;

Bool   : 'true' | 'false' ;
Ident  : [A-Za-z_][A-Za-z0-9_]* ;
WS     : [ \t]+ -> skip ;
NL     : '\r'? '\n' ;
Number : [+-]?(DIGIT+('.'DIGIT*)?|'.'DIGIT+)([eE][-+]?DIGIT+)? ;
String : '"'PRINTABLE*'"' ;
FunctionCall : Ident'(' ;
ExternalFunctionCall : Ident'.'Ident'(' ;   // no spaces between tokens
