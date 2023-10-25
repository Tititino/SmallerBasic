grammar SBGrammar;
@header {
package smallerbasic;
}

program : (statement | subroutineDecl)* EOF
        ;

assignmentStmt : var=variable '=' expr=expression
               ;

variable : name=Ident                                   # Var
         | name=Ident ('[' expr+=arithExpression ']')+  # Array
         ;

label : Ident ':' ;

statement : assignmentStmt
          | ifStmt
          | forStmt
          | whileStmt
          | gotoStmt
          | callRoutine
          | callExternalFunction
          | label
          ;

callRoutine : FunctionCall ;

callExternalFunction : name=ExternalFunctionCall args+=expression? (',' args+=expression)* ')' ;

subroutineDecl : 'Sub' name=Ident
                     body+=statement*
                 'EndSub' ;

expression : variable
           | arithExpression
           | stringExpression
           | booleanExpression
           ;

ifStmt : 'If' '(' cond=booleanExpression ')' 'Then'
             bodyTrue+=statement*
          ('Else'
             bodyFalse+=statement*
          )?
          'EndIf'
       ;

forStmt : 'For' var=Ident '=' from=arithExpression 'To' to=arithExpression ('Step' step=arithExpression)?
              body+=statement*
          'EndFor'
        ;

whileStmt : 'While' '(' cond=booleanExpression ')'
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
WS     : [ \t\r\n]+ -> skip ;
FunctionCall : Ident'('WS*')' ;             // no spaces between brackets
ExternalFunctionCall : Ident'.'Ident'(' ;   // no spaces between brackets
