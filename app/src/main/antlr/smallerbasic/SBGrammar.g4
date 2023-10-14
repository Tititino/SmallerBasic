grammar SBGrammar;
@header {
package smallerbasic;
}

program : ( statement | subroutineDecl NL )* ;

assignmentStmt : Ident '=' expression
               | Ident '=' Ident
               ;

label : Ident ':';

statement : assignmentStmt  NL
          | ifStmt          NL
          | forStmt         NL
          | whileStmt       NL
          | gotoStmt        NL
          | expression      NL
          | label           NL
          ;

callRoutine : FunctionCall ')'
            | ExternalFunctionCall expression? (',' expression)* ')'
            ;

subroutineDecl : 'Sub' Ident statement* 'EndSub' ;

expression : callRoutine
           | arithExpression
           | booleanExpression
           ;

ifStmt : 'If' '(' booleanExpression ')' 'Then' statement* 'EndIf'
       | 'If' '(' booleanExpression ')' 'Then' statement* 'Else' statement* 'EndIf'
       ;

forStmt : 'For' Ident '=' arithExpression 'To' arithExpression statement* 'EndFor'
        | 'For' Ident '=' arithExpression 'To' arithExpression 'Step' arithExpression statement* 'EndFor'
        ;

whileStmt : 'While' '(' booleanExpression ')' statement* 'EndWhile' ;

gotoStmt  : 'Goto' Ident ;

booleanExpression : arithExpression ('<='|'='|'<>'|'<'|'>'|'>=') arithExpression
                  | booleanExpression ('And'|'Or') booleanExpression
                  | '(' booleanExpression ')'
                  | Bool
                  | callRoutine
                  | Ident
                  ;

arithExpression : arithExpression ('/' | '*') arithExpression
                | arithExpression ('+' | '-') arithExpression
                | '(' arithExpression ')'
                | String
                | Number
                | callRoutine
                | Ident
                ;

fragment DIGIT : [0-9] ;
fragment PRINTABLE : ~["] ;

Ident  : [A-Za-z_][A-Za-z0-9_]* ;
WS     : [ \t]+ -> skip ;
NL     : '\r'? '\n' ;     // return newlines to parser (is end-statement signal)
Number : [+-]?(DIGIT+('.'DIGIT*)?|'.'DIGIT+)([eE][-+]?DIGIT+)? ;
String : '"'PRINTABLE*'"' ;
FunctionCall : Ident'(' ;
ExternalFunctionCall : Ident'.'Ident'(' ;   // no spaces between tokens
Bool   : 'true' | 'false' ;