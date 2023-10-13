grammar SBGrammar;
@header {
package smallerbasic;
}

assignmentStmt : Ident '=' arithExpression
               | Ident '=' booleanExpression
               | Ident '=' Ident
               ;

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