/**
 * Quil Parser & Analyser
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2025 Lian Remme <lian.remme@dlr.de>
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

package de.hhu.lirem101.quil_optimizer.quil_variable;

import java.util.*;

public class RelevantNodeRules {
    // Gates that help deciding on the usage type of a classical variable
    public static HashMap<String, List<ClassicalUsage>> classicalUsage() {
        HashMap<String, List<ClassicalUsage>> gateUsage = new HashMap<>();
        gateUsage.put("memoryDescriptor", Collections.singletonList(ClassicalUsage.DECLARE));
        gateUsage.put("NEG", Collections.singletonList(ClassicalUsage.USAGE_ASSIGNMENT));
        gateUsage.put("NOT", Collections.singletonList(ClassicalUsage.USAGE_ASSIGNMENT));
        gateUsage.put("TRUE", Collections.singletonList(ClassicalUsage.ASSIGNMENT));
        gateUsage.put("FALSE", Collections.singletonList(ClassicalUsage.ASSIGNMENT));
        gateUsage.put("logicalBinaryOp", Arrays.asList(ClassicalUsage.USAGE_ASSIGNMENT, ClassicalUsage.USAGE));
        gateUsage.put("arithmeticBinaryOp", Arrays.asList(ClassicalUsage.USAGE_ASSIGNMENT, ClassicalUsage.USAGE));
        gateUsage.put("move", Arrays.asList(ClassicalUsage.ASSIGNMENT, ClassicalUsage.USAGE));
        gateUsage.put("exchange", Arrays.asList(ClassicalUsage.USAGE_ASSIGNMENT, ClassicalUsage.USAGE_ASSIGNMENT));
        gateUsage.put("convert", Arrays.asList(ClassicalUsage.ASSIGNMENT, ClassicalUsage.USAGE));
        gateUsage.put("classicalComparison", Arrays.asList(ClassicalUsage.ASSIGNMENT, ClassicalUsage.USAGE, ClassicalUsage.USAGE));
        return gateUsage;
    }

    public static Set<String> measurementNodes() {
        Set<String> quantumNodes = new HashSet<>();
        quantumNodes.add("measure");
        quantumNodes.add("circuitMeasure");
        return quantumNodes;
    }
}
