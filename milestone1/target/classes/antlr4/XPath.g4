grammar XPath ;

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

constant
	: '"' NAME '"'
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

DOC: D O C;
fragment D: [dD];
fragment O: [oO];
fragment C: [cC];
LPR: '(';
RPR: ')';
NAME: [a-zA-Z0-9_-]+;
TXT: 'text()';
WS : [ \t\r\n]+ -> skip;
