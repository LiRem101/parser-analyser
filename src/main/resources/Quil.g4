/*
 ANTLR4 grammar taken from Quil GitHub repository:
 https://github.com/quil-lang/quil/blob/master/grammars/Quil.g4

 The keyword "wait" was changed to "wait1" to avoid conflicts with the Antlr implementation.
 As inmemantlr creates method names out of the rule names, and the name "wait" for a method is already reserved in Java.

 SPDX-FileCopyrightText: 2024 Robert S. Smith; Rigetti & Co. Inc.; and contributors

 SPDX-License-Identifier: Apache-2.0
*/

grammar Quil;

////////////////////
// PARSER
////////////////////

quil                : allInstr? ( NEWLINE+ allInstr )* NEWLINE* EOF ;

allInstr            : defGate
                    | defCircuit
                    | instr
                    ;

instr               : gate
                    | measure
                    | defLabel
                    | halt
                    | jump
                    | jumpWhen
                    | jumpUnless
                    | resetState
                    | wait1
                    | classicalUnary
                    | classicalBinary
                    | classicalComparison
                    | load
                    | store
                    | nop
                    | include
                    | pragma
                    | memoryDescriptor
                    | extern
                    | call
                    ;

// C. Static and Parametric Gates

gate                : modifier* name ( LPAREN param ( COMMA param )* RPAREN )? qubit+ ;

name                : IDENTIFIER ;
qubit               : INT ;

param               : expression ;

modifier            : CONTROLLED
                    | DAGGER ;

// D. Gate Definitions

defGate             : DEFGATE name ( ( LPAREN variable ( COMMA variable )* RPAREN ) | ( AS gateType ) )? COLON NEWLINE matrix ;

variable            : PERCENTAGE IDENTIFIER ;

gateType            : MATRIX | PERMUTATION ;

matrix              : ( matrixRow NEWLINE )* matrixRow ;
matrixRow           : TAB expression ( COMMA expression )* ;

// E. Circuits

defCircuit          : DEFCIRCUIT name ( LPAREN variable ( COMMA variable )* RPAREN )? qubitVariable* COLON NEWLINE circuit ;

qubitVariable       : IDENTIFIER ;

circuitQubit        : qubit | qubitVariable ;
circuitGate         : name ( LPAREN param ( COMMA param )* RPAREN )? circuitQubit+ ;
circuitMeasure      : MEASURE circuitQubit addr? ;
circuitResetState   : RESET circuitQubit? ;
circuitInstr        : circuitGate | circuitMeasure | circuitResetState | instr ;
circuit             : ( TAB circuitInstr NEWLINE )* TAB circuitInstr ;

// F. Measurement

measure             : MEASURE qubit addr? ;
addr                : IDENTIFIER | ( IDENTIFIER? LBRACKET INT RBRACKET );

// G. Program control

defLabel            : LABEL label ;
label               : AT IDENTIFIER ;
halt                : HALT ;
jump                : JUMP label ;
jumpWhen            : JUMPWHEN label addr ;
jumpUnless          : JUMPUNLESS label addr ;

// H. Zeroing the Quantum State

resetState          : RESET qubit? ; // NB: cannot be named "reset" due to conflict with Antlr implementation

// I. Classical/Quantum Synchronization

wait1               : WAIT ;

// J. Classical Instructions

memoryDescriptor    : DECLARE IDENTIFIER IDENTIFIER ( LBRACKET INT RBRACKET )? ( SHARING IDENTIFIER ( offsetDescriptor )* )? ;
offsetDescriptor    : OFFSET INT IDENTIFIER ;

classicalUnary      : ( NEG | NOT | TRUE | FALSE ) addr ;
classicalBinary     : logicalBinaryOp | arithmeticBinaryOp | move | exchange | convert ;
logicalBinaryOp     : ( AND | OR | IOR | XOR ) addr ( addr | INT ) ;
arithmeticBinaryOp  : ( ADD | SUB | MUL | DIV ) addr ( addr | number ) ;
move                : MOVE addr ( addr | number );
exchange            : EXCHANGE addr addr ;
convert             : CONVERT addr addr ;
load                : LOAD addr IDENTIFIER addr ;
store               : STORE IDENTIFIER addr ( addr | number );
classicalComparison : ( EQ | GT | GE | LT | LE ) addr addr ( addr | number );

// K. The No-Operation Instruction

nop                 : NOP ;

// L. File Inclusion

include             : INCLUDE STRING ;

// M. Pragma Support

pragma              : PRAGMA IDENTIFIER pragma_name* STRING? ;
pragma_name         : IDENTIFIER | INT ;


// N. Declaring and Calling External Functions

extern              : EXTERN IDENTIFIER ;
call                : CALL IDENTIFIER call_arg+ ;
call_arg            : addr | number ;

// Expressions (in order of precedence)

expression          : LPAREN expression RPAREN                  #parenthesisExp
                    | sign expression                           #signedExp
                    | <assoc=right> expression POWER expression #powerExp
                    | expression ( TIMES | DIVIDE ) expression  #mulDivExp
                    | expression ( PLUS | MINUS ) expression    #addSubExp
                    | function LPAREN expression RPAREN         #functionExp
                    | number                                    #numberExp
                    | variable                                  #variableExp
                    | addr                                      #addrExp
                    ;

function            : SIN | COS | SQRT | EXP | CIS | IDENTIFIER;
sign                : PLUS | MINUS ;

// Numbers
// We suffix -N onto these names so they don't conflict with already defined Python types

number              : MINUS? ( realN | imaginaryN | I | PI ) ;
imaginaryN          : realN I ;
realN               : FLOAT | INT ;

////////////////////
// LEXER
////////////////////

// Keywords

DEFGATE             : 'DEFGATE' ;
DEFCIRCUIT          : 'DEFCIRCUIT' ;
MEASURE             : 'MEASURE' ;

LABEL               : 'LABEL' ;
HALT                : 'HALT' ;
JUMP                : 'JUMP' ;
JUMPWHEN            : 'JUMP-WHEN' ;
JUMPUNLESS          : 'JUMP-UNLESS' ;

RESET               : 'RESET' ;
WAIT                : 'WAIT' ;
NOP                 : 'NOP' ;
INCLUDE             : 'INCLUDE' ;
PRAGMA              : 'PRAGMA' ;

DECLARE             : 'DECLARE' ;
SHARING             : 'SHARING' ;
OFFSET              : 'OFFSET' ;

NEG                 : 'NEG' ;
NOT                 : 'NOT' ;
TRUE                : 'TRUE' ; // Deprecated
FALSE               : 'FALSE' ; // Deprecated

AND                 : 'AND' ;
IOR                 : 'IOR' ;
XOR                 : 'XOR' ;
OR                  : 'OR' ;   // Deprecated

ADD                 : 'ADD' ;
SUB                 : 'SUB' ;
MUL                 : 'MUL' ;
DIV                 : 'DIV' ;

MOVE                : 'MOVE' ;
EXCHANGE            : 'EXCHANGE' ;
CONVERT             : 'CONVERT' ;

EQ                  : 'EQ';
GT                  : 'GT';
GE                  : 'GE';
LT                  : 'LT';
LE                  : 'LE';

LOAD                : 'LOAD' ;
STORE               : 'STORE' ;

PI                  : 'pi' ;
I                   : 'i' ;

SIN                 : 'SIN' ;
COS                 : 'COS' ;
SQRT                : 'SQRT' ;
EXP                 : 'EXP' ;
CIS                 : 'CIS' ;

MATRIX              : 'MATRIX' ;
PERMUTATION         : 'PERMUTATION' ;

// Operators

PLUS                : '+' ;
MINUS               : '-' ;
TIMES               : '*' ;
DIVIDE              : '/' ;
POWER               : '^' ;

// Modifiers

CONTROLLED          : 'CONTROLLED' ;
DAGGER              : 'DAGGER' ;

// Identifiers

IDENTIFIER          : ( ( [A-Za-z_] ) | ( [A-Za-z_] [A-Za-z0-9\-_]* [A-Za-z0-9_] ) ) ;

// Numbers

INT                 : [0-9]+ ;
FLOAT               : [0-9]+ ( '.' [0-9]+ )? ( ( 'e'|'E' ) ( '+' | '-' )? [0-9]+ )? ;

// String

STRING              : '"' ~( '\n' | '\r' )* '"';

// Punctuation

PERIOD              : '.' ;
COMMA               : ',' ;
LPAREN              : '(' ;
RPAREN              : ')' ;
LBRACKET            : '[' ;
RBRACKET            : ']' ;
COLON               : ':' ;
PERCENTAGE          : '%' ;
AT                  : '@' ;
QUOTE               : '"' ;
UNDERSCORE          : '_' ;

// Whitespace

TAB                 : '    ' ;
NEWLINE             : (' ' | '\t' )* ( '\r'? '\n' | '\r' )+ ;

// Skips

COMMENT             : (' ' | '\t' )* '#' ~( '\n' | '\r' )* -> skip ;
SPACE               : ' ' -> skip ;

// Error

INVALID             : . ;
