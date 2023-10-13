grammar SBGrammar;
@header {
package smallerbasic;
}

literal : Number
        | String
        | Bool
        ;

expression : expression ('/' | '*') expression
           | expression ('+' | '-') expression
           | '(' expression ')'
           | literal
           ;

fragment DIGIT : [0-9] ;
fragment PRINTABLE : ~["] ;
WS : [ \r\t\n]+ -> skip ;
Number : [+-]?(DIGIT+('.'DIGIT*)?|'.'DIGIT+)([eE][-+]?DIGIT+)? ;
String : '"'PRINTABLE*'"' ;
Bool   : 'true' | 'false' ;
