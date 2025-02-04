/*
 SPDX-FileCopyrightText: 2016 Julian Thome <julian.thome.de@gmail.com>
 SPDX-License-Identifier: MIT
*/

grammar Hello;
r   : 'hello' ID;
ID  : [a-z]+ ;
WS  : [ \t\r\n]+ -> skip ;