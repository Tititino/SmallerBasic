grammar SBGrammar;
@header {
package smallerbasic;
}

program : (statement | subroutineDecl | NL)* EOF
        ;

assignmentStmt : var=variable '=' expr=expression
               ;

variable : name=Ident                                   # Var
         | name=ArrayAccess expr=arithExpression ']'    # Array
         ;

label : Ident ':' ;

statement : assignmentStmt          NL
          | ifStmt                  NL
          | forStmt                 NL
          | whileStmt               NL
          | gotoStmt                NL
          | callRoutine             NL
          | callExternalFunction    NL
          | label                   NL
          ;

callRoutine : FunctionCall ;

callExternalFunction : name=ExternalFunctionCall args+=expression? (',' args+=expression)* ')' ;

subroutineDecl : 'Sub' name=Ident NL
                     body+=statement*
                 'EndSub' NL ;

expression : variable
           | arithExpression
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
                  | callExternalFunction                                                                # BoolReturningFunc
                  | variable                                                                            # BoolVar
                  ;

stringExpression : left=stringExpression '+' right=stringExpression                         # StringConcat
                 | '(' expr=stringExpression ')'                                            # SParens
                 | String                                                                   # StringLiteral
                 | callExternalFunction                                                     # StrReturningFunc
                 | variable                                                                 # StrVar
                 ;

arithExpression : left=arithExpression op=('/' | '*') right=arithExpression                 # DivMul
                | left=arithExpression op=('+' | '-') right=arithExpression                 # PlusMin
                | '(' expr=arithExpression ')'                                              # NParens
                | op='-' var=Ident                                                          # MinusVar
                | op='-' '(' expr=arithExpression ')'                                       # UnaryMinus
                | Number                                                                    # NumberLiteral
                | callExternalFunction                                                      # NumberReturningFunc
                | variable                                                                  # NumberVar
                ;

fragment DIGIT : [0-9] ;
fragment PRINTABLE : ~["] ;

Bool   : 'true' | 'false' ;
Number : [+-]?(DIGIT+('.'DIGIT*)?|'.'DIGIT+)([eE][-+]?DIGIT+)? ;
String : '"'PRINTABLE*'"' ;
Ident  : [A-Za-z_][A-Za-z0-9_]* ;
WS     : [ \t]+ -> skip ;
NL     : '\r'? '\n' ;
FunctionCall : Ident'('WS*')' ;             // no spaces between brackets
ExternalFunctionCall : Ident'.'Ident'(' ;   // no spaces between brackets
ArrayAccess : Ident'[' ;                    // no spaces between brackets
