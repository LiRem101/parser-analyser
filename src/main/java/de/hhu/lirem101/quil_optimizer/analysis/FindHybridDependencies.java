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

package de.hhu.lirem101.quil_optimizer.analysis;

import de.hhu.lirem101.quil_analyser.LineType;
import de.hhu.lirem101.quil_optimizer.InstructionNode;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import java.util.*;

/**
 * This class is used to find hybrid dependencies in a list of lists of instructions.
 */
public class FindHybridDependencies {

    private final ArrayList<ArrayList<InstructionNode>> instructions;
    private final ArrayList<LinkedHashMap<Integer, Set<Integer>>> hybridDependencyList = new ArrayList<>();
    private boolean calculated = false;

    /**
     * Constructor for FindHybridDependencies.
     * @param instructions The list of list of instructions to be analyzed.
     */
    public FindHybridDependencies(ArrayList<ArrayList<InstructionNode>> instructions) {
        this.instructions = instructions;
    }

    /**
     * Saves hybrid Nodes in hybrid Dependencies.
     */
    private void findHybridNodes() {
        for (ArrayList<InstructionNode> instructionList : instructions) {
            LinkedHashMap<Integer, Set<Integer>> hybridDependencies = new LinkedHashMap<>();
            Set<Integer> handledLines = new HashSet<>();
            ArrayList<InstructionNode> hybridNodes = (ArrayList<InstructionNode>) instructionList.stream()
                    .filter(instruction -> instruction.getLineType() != LineType.CLASSICAL && instruction.getLineType() != LineType.QUANTUM)
                    .collect(java.util.stream.Collectors.toList());
            for (InstructionNode hybridNode : hybridNodes) {
                int line = hybridNode.getCodelines().get(0);
                Set<InstructionNode> dep = hybridNode.getDependencies();
                Set<Integer> dependentLines = dep.stream()
                        .flatMap(x -> x.getCodelines().stream())
                        .filter(x -> !handledLines.contains(x))
                        .collect(java.util.stream.Collectors.toCollection(HashSet::new));
                handledLines.addAll(dependentLines);
                hybridDependencies.put(line, dependentLines);
            }

            hybridDependencyList.add(hybridDependencies);
        }
    }

    /**
     * Returns the hybrid dependencies as list.
     * @return The hybrid dependencies.
     */
    public ArrayList<LinkedHashMap<Integer, Set<Integer>>> getHybridDependencies() {
        if (!calculated) {
            findHybridNodes();
            calculated = true;
        }
        return hybridDependencyList;
    }

    /**
     * Add information about hybrid dependencies into JsonArrayBuilder and return it.
     * @return The JsonArrayBuilder with the applied information.
     */
    public JsonArrayBuilder addDeadVariablesToJson() {
        if (!calculated) {
            findHybridNodes();
            calculated = true;
        }
        JsonArrayBuilder hybridDependencies = Json.createArrayBuilder();
        for (Map<Integer, Set<Integer>> hybridDependency : hybridDependencyList) {
            JsonObjectBuilder hybridDependencyJson = Json.createObjectBuilder();
            for (Map.Entry<Integer, Set<Integer>> entry : hybridDependency.entrySet()) {
                JsonArrayBuilder dependentLines = Json.createArrayBuilder();
                for (Integer line : entry.getValue()) {
                    dependentLines.add(line);
                }
                hybridDependencyJson.add(entry.getKey().toString(), dependentLines);
            }
            hybridDependencies.add(hybridDependencyJson);
        }
        return hybridDependencies;
    }
}
