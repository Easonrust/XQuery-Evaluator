grammar XQuery ;

@header {
package edu.ucsd.CSE232B.parsers;
}

/*Rules*/
ap
	: doc '/' rp                   # ApChildren
	| doc '//' rp                  # ApAll
	;

doc
	: DOC LPR fname RPR
	;

fname
	: STRING
	;

constant
	: STRING
	;

rp
	: NAME                         # TagName
	| '*'                          # AllChildren
	| '.'                          # Current
	| '..'                         # Parent
	| TXT                          # Text
	| '@' NAME                     # Attribute
	| LPR rp RPR                   # RpwithP
	| rp '/' rp                    # RpChildren
	| rp '//' rp                   # RpAll
	| rp '[' filter ']'            # RpFilter
	| rp ',' rp                    # TwoRp
	;

filter
	: rp                           # FilterRp
	| rp '=' rp                    # FilterEqual
	| rp 'eq' rp                   # FilterEqual
	| rp '==' rp                   # FilterIs
	| rp 'is' rp                   # FilterIs
	| rp '=' constant              # FilterConstant
	| LPR filter RPR               # FilterwithP
	| filter 'and' filter          # FilterAnd
	| filter 'or' filter           # FilterOr
	| 'not' filter                 # FilterNot
	;

xq
    :   var                                                 # XQVar
    |   constant                                            # XQStrConst
    |   ap                                                  # XQAp
    |   LPR xq RPR                                          # XQwithP
    |   xq '/' rp                                           # XQRp
    |   xq '//' rp                                          # XQRpAll
    |   '<' NAME '>' '{' xq '}' '<''/' NAME '>'             # XQTag
    |   xq ',' xq                                           # TwoXQ
    |   forClause letClause? whereClause? returnClause      # XQFLWR
    |   letClause xq                                        # XQLet
    |   joinClause                                          # XQJoin
    ;

var
    : '$' NAME
    ;

forClause
    : 'for' (var 'in' xq ',' )* var 'in' xq
    ;

letClause
    :'let' (var ':=' xq ',' )* var ':=' xq
    ;

whereClause
    : 'where' cond
    ;

returnClause
    : 'return' xq
    ;

joinClause
    :  'join' '(' xq ',' xq ',' attr ',' attr')'
    ;

attr
    : '[' NAME ? (',' NAME )* ']'
    ;

cond
    :   xq '=' xq                 # CondEqual
    |   xq 'eq' xq                # CondEqual
    |   xq '==' xq                # CondIs
    |   xq 'is' xq                # CondIs
    |   'empty' '(' xq ')'        # CondEmpty
    |   'some' var 'in' xq (',' var 'in' xq)* 'satisfies' cond  # CondSome
    |   '(' cond ')'              # CondWithPar
    |   cond 'and' cond           # CondAnd
    |   cond 'or' cond            # CondOr
    |   'not' cond                # CondNot
    ;



/*Tokens*/
STRING
:
   '"'
   (
      ESCAPE
      | ~["\\]
   )* '"'
   | '\''
   (
      ESCAPE
      | ~['\\]
   )* '\''
;

ESCAPE
:
   '\\'
   (
      ['"\\]
   )
;

DOC: D O C | 'document';
fragment D: [dD];
fragment O: [oO];
fragment C: [cC];
LPR: '(';
RPR: ')';
NAME: [a-zA-Z0-9_-]+;
TXT: 'text()';
WS : [ \t\r\n]+ -> skip;
