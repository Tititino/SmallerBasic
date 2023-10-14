grammar SBGrammar;
@header {
package smallerbasic;
}

assignmentStmt : Ident '=' arithExpression
               | Ident '=' booleanExpression
               | Ident '=' Ident
               ;

label : Ident ':';

program : statement* ;

statement : assignmentStmt
          | ifStmt
          | forStmt
          | whileStmt
          | gotoStmt
          | label
          ;

ifStmt : 'If' '(' booleanExpression ')' 'Then' statement* 'EndIf'
       | 'If' '(' booleanExpression ')' 'Then' statement* 'Else' statement* 'EndIf'
       ;

forStmt : 'For' Ident '=' arithExpression 'To' arithExpression statement* 'EndFor'
        | 'For' Ident '=' arithExpression 'To' arithExpression 'Step' statement* 'EndFor'
        ;

whileStmt : 'While' '(' booleanExpression ')' statement* 'EndFor' ;

gotoStmt  : 'Goto' Ident ;

booleanExpression : arithExpression ('<='|'='|'<>'|'<'|'>'|'>=') arithExpression
                  | booleanExpression ('And'|'Or') booleanExpression
                  | '(' booleanExpression ')'
                  | Bool
                  | Ident
                  ;

arithExpression : arithExpression ('/' | '*') arithExpression
                | arithExpression ('+' | '-') arithExpression
                | '(' arithExpression ')'
                | String
                | Number
                | Ident
                ;

fragment DIGIT : [0-9] ;
fragment PRINTABLE : ~["] ;

Ident  : [A-Za-z_][A-Za-z0-9_]* ;
WS     : [ \r\t\n]+ -> skip ;
Number : [+-]?(DIGIT+('.'DIGIT*)?|'.'DIGIT+)([eE][-+]?DIGIT+)? ;
String : '"'PRINTABLE*'"' ;
Bool   : 'true' | 'false' ;