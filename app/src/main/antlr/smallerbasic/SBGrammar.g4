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

subroutineDecl : 'Sub' name=Ident body+=statement* 'EndSub' NL ;

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

booleanExpression : arithExpression relop=('<='|'='|'<>'|'<'|'>'|'>=') arithExpression
                  | stringExpression relop=('<='|'='|'<>'|'<'|'>'|'>=') stringExpression
                  | booleanExpression binop=('And'|'Or') booleanExpression
                  | '(' booleanExpression ')'
                  | Bool
                  | Ident
                  ;

stringExpression : stringExpression '+' stringExpression
                 | '(' stringExpression ')'
                 | String
                 | Ident
                 ;

arithExpression : arithExpression op=('/' | '*') arithExpression
                | arithExpression op=('+' | '-') arithExpression
                | '(' arithExpression ')'
                | Number
                | Ident
                ;

fragment DIGIT : [0-9] ;
fragment PRINTABLE : ~["] ;

Ident  : [A-Za-z_][A-Za-z0-9_]* ;
WS     : [ \t]+ -> skip ;
NL     : '\r'? '\n' ;
Number : [+-]?(DIGIT+('.'DIGIT*)?|'.'DIGIT+)([eE][-+]?DIGIT+)? ;
String : '"'PRINTABLE*'"' ;
FunctionCall : Ident'(' ;
ExternalFunctionCall : Ident'.'Ident'(' ;   // no spaces between tokens
Bool   : 'true' | 'false' ;