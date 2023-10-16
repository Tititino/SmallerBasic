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

statement : assignmentStmt  NL  # AssignmentStatment
          | ifStmt          NL  # IfStatement
          | forStmt         NL  # ForStatement
          | whileStmt       NL  # WhileStatement
          | gotoStmt        NL  # GotoStatement
          | callRoutine     NL  # CallRoutineStatement
          | label           NL  # LabelStatement
          ;

callRoutine : FunctionCall ')'
            | ExternalFunctionCall args+=expression? (',' args+=expression)* ')'
            ;

subroutineDecl : 'Sub' name=Ident body=statement* 'EndSub' NL ;

expression : arithExpression
           | stringExpression
           | booleanExpression
           ;

ifStmt : 'If' '(' cond=booleanExpression ')' 'Then' NL
            bodyTrue=statement*
          ('Else' NL
            bodyFalse=statement*
          )?
          'EndIf'
       ;

forStmt : 'For' var=Ident '=' from=arithExpression 'To' to=arithExpression ('Step' arithExpression)? NL
            body=statement*
          'EndFor'
        ;

whileStmt : 'While' '(' cond=booleanExpression ')' NL
                body=statement*
            'EndWhile'
          ;

gotoStmt  : 'Goto' lbl=Ident
          ;

booleanExpression : arithExpression relop=('<='|'='|'<>'|'<'|'>'|'>=') arithExpression      # NumberComparison
                  | stringExpression relop=('<='|'='|'<>'|'<'|'>'|'>=') stringExpression    # StringComparison
                  | booleanExpression binop=('And'|'Or') booleanExpression                  # Boolean
                  | '(' booleanExpression ')'                                               # BParens
                  | Bool                                                                    # BoolLiteral
                  | Ident                                                                   # BoolIdentifier
                  ;

stringExpression : stringExpression '+' stringExpression                                    # ConcatExpr
                 | '(' stringExpression ')'                                                 # SParen
                 | String                                                                   # StrLiteral
                 | Ident                                                                    # StrIdentifier
                 ;

arithExpression : arithExpression op=('/' | '*') arithExpression    # MulDiv
                | arithExpression op=('+' | '-') arithExpression    # PlusMinus
                | '(' arithExpression ')'                           # AParens
                | Number                                            # NumberLiteral
                | Ident                                             # NumIdentifier
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