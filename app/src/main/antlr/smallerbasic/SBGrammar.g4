grammar SBGrammar;
@header {
package smallerbasic;
}

literal : Number
        | String
        | Bool
        ;

booleanExpression : expression ('<='|'='|'<>'|'<'|'>'|'>=') expression
                  | booleanExpression ('And'|'Or') booleanExpression
                  | '(' booleanExpression ')'
                  | Bool
                  | Ident
                  ;

expression : expression ('/' | '*') expression
           | expression ('+' | '-') expression
           | '(' expression ')'
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