/**
 * Quil Parser & Analyser
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2025 Lian Remme <lian.remme@dlr.de>
 *
 * SPDX-FileCopyrightText: 2025 Lian Remme <lian.remme@dlr.de>
 *
 * SPDX-License-Identifier: MIT
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 **/

package de.hhu.lirem101.quil_analyser;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * This class contains the rules for the parse tree.
 */
public class RulesOfParseTree {
    // Gate: Depends on param, just like circuitGate
    // Measure only influences classical if addr is given
    // Param (expression) classical (or rather C -> Q) if it is not exclusively a number
    // defCircuit is a control structure, but its children have to be checked as well

    public static final Set<String> quantum = Collections.unmodifiableSet(new HashSet<>(Arrays.asList("defGate", "qubitVariable", "circuitQubit", "resetState", "circuitResetState")));
    public static final Set<String> classical = Collections.unmodifiableSet(new HashSet<>(Arrays.asList("classicalUnary", "classicalBinary", "classicalComparison", "load", "store",
            "memoryDescriptor")));
    public static final Set<String> quantumInfluClassical = Collections.unmodifiableSet(new HashSet<>(Arrays.asList("measure", "circuitMeasure")));
    public static final Set<String> controlStructure = Collections.unmodifiableSet(new HashSet<>(Arrays.asList("defLabel", "halt", "jump")));
    public static final Set<String> controlStructureClassical = Collections.unmodifiableSet(new HashSet<>(Arrays.asList("jumpWhen", "jumpUnless")));

}
