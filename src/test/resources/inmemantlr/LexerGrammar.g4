/*
 SPDX-FileCopyrightText: 2016 Julian Thome <julian.thome.de@gmail.com>
 SPDX-License-Identifier: MIT
*/

lexer grammar LexerGrammar;

RULE : [a-z] DIGIT;

DIGIT: [0-9]+;

WS:  [ \t\r\n\u000C]+ -> skip
  ;
