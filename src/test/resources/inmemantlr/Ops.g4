/*
 SPDX-FileCopyrightText: 2016 Julian Thome <julian.thome.de@gmail.com>
 SPDX-License-Identifier: MIT
*/

grammar Ops;

Plus: '+';
Minus: '-';
Number: '-'?([0-9]|[1-9][0-9]+);

s: (expression)* EOF;
plus: Plus;
minus: Minus;
operation: plus | minus;
expression: operand operation operand;
operand: Number;

WS  :  [ \t\r\n]+ -> skip;