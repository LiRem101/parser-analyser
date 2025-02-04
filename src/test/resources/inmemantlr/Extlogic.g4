/*
 SPDX-FileCopyrightText: 2016 Julian Thome <julian.thome.de@gmail.com>
 SPDX-License-Identifier: MIT
*/

grammar ExtLogic;

import Logic;

test : expression | 'hello' | IDENTIFIER;

