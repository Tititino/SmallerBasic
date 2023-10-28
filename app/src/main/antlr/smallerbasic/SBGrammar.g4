grammar SBGrammar;
@header {
package smallerbasic;
}

program : (statement | subroutineDecl)* EOF
        ;

assignmentStmt : var=variable Equal expr=expression
               ;

variable : name=varName                                   # Var
         | name=varName ('[' expr+=arithExpression ']')+  # Array
         ;

label : labelName ':' ;

varName : Ident ;

labelName : Ident ;

functionName : Ident ;

statement : assignmentStmt
          | ifStmt
          | forStmt
          | whileStmt
          | gotoStmt
          | callRoutine
          | callExternalFunction
          | label
          ;

callRoutine : functionName '(' ')' ;

callExternalFunction : name=ExternalFunctionName '(' args+=expression? (',' args+=expression)* ')' ;

subroutineDecl : 'Sub' name=functionName
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

forStmt : 'For' var=variable Equal from=arithExpression 'To' to=arithExpression ('Step' step=arithExpression)?
              body+=statement*
          'EndFor'
        ;

whileStmt : 'While' '(' cond=booleanExpression ')'
                body+=statement*
            'EndWhile'
          ;

gotoStmt  : 'Goto' lbl=labelName
          ;

booleanExpression : left=arithExpression   relop=(Relop|Equal) right=arithExpression           # NumberComparison
                  | left=stringExpression  relop=(Relop|Equal) right=stringExpression          # StringComparison
                  | left=booleanExpression binop=Boolop right=booleanExpression         # BoolOp
                  | '(' expr=booleanExpression ')'                                      # BParens
                  | Bool                                                                # BoolLiteral
                  | callExternalFunction                                                # BoolReturningFunc
                  | variable                                                            # BoolVar
                  ;

stringExpression : left=stringExpression '+' right=stringExpression                         # StringConcat
                 | '(' expr=stringExpression ')'                                            # SParens
                 | String                                                                   # StringLiteral
                 | callExternalFunction                                                     # StrReturningFunc
                 | variable                                                                 # StrVar
                 ;

arithExpression : left=arithExpression op=('/' | '*') right=arithExpression                 # DivMul
                | left=arithExpression op=('+' | '-') right=arithExpression                 # PlusMin
                | op='-' arithAtom                                                          # UnaryMinus
                | arithAtom                                                                 # Atom
                ;

arithAtom : variable                                                                        # VariableAtom
          | callExternalFunction                                                            # ExternalFuncAtom
          | '(' arithExpression ')'                                                         # ParensAtom
          | Number                                                                          # LiteralAtom
          ;

fragment DIGIT : [0-9] ;
fragment PRINTABLE : ~["] ;

Bool   : 'true' | 'false' ;
Equal  : '=' ;
Relop  : ('<='|'<>'|'<'|'>'|'>=') ;
Boolop : ('And'|'Or') ;
Number : [+-]?(DIGIT+('.'DIGIT*)?|'.'DIGIT+)([eE][-+]?DIGIT+)? ;
String : '"'PRINTABLE*'"' ;
ExternalFunctionName : Ident'.'Ident ;
Ident  : [A-Za-z_][A-Za-z0-9_]* ;
WS     : [ \t\r\n]+ -> skip ;
