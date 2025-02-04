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

package de.hhu.lirem101.quil_optimizer.transformation;

import de.hhu.lirem101.quil_analyser.LineType;
import de.hhu.lirem101.quil_optimizer.ExecutableInstructionsExtractor;
import de.hhu.lirem101.quil_optimizer.InstructionNode;

import java.util.*;

public class LatestPossibleQuantumExecuter {

    LinkedHashMap<Integer, Set<Integer>> hybridDependencies;
    ArrayList<InstructionNode> instructions;
    boolean calculated = false;
    boolean orderRemainedEqual = false;

    public LatestPossibleQuantumExecuter(LinkedHashMap<Integer, Set<Integer>> hybridDependencies, ArrayList<InstructionNode> instructions) {
        this.hybridDependencies = hybridDependencies;
        this.instructions = instructions;
    }

    public ArrayList<InstructionNode> reorderInstructions() {
        if(hybridDependencies.isEmpty()) {
            return new ArrayList<>();
        }
        if(!calculated) {
            reOrderAllInstructions();
            calculated = true;
        }

        if(orderRemainedEqual) {
            return new ArrayList<>();
        }

        return this.instructions;
    }

    private void reOrderAllInstructions() {
        Set<Integer> firstDependencies = hybridDependencies.values().stream().findFirst().orElse(null);
        if (firstDependencies == null) {
            return;
        }
        int firstHybridLine = hybridDependencies.keySet().stream()
                .filter(x -> hybridDependencies.get(x).equals(firstDependencies))
                .findFirst()
                .orElse(-1);
        if (firstHybridLine == -1) {
            return;
        }
        ArrayList<InstructionNode> executableClassicalInstructions = executableClassicalInstrutionsWithoutQuantum();

        reOrder(executableClassicalInstructions, firstHybridLine);

        orderRemainedEqual = executableClassicalInstructions.equals(instructions);
        instructions = executableClassicalInstructions;
    }

    private void reOrder(ArrayList<InstructionNode> executableClassicalInstructions, int firstHybridLine) {
        InstructionNode hybridInstruction = instructions.stream()
                .filter(x -> x.getLine() == firstHybridLine)
                .findFirst()
                .orElse(null);
        if(hybridInstruction == null) {
            return;
        }
        Set<InstructionNode> necessaryNodes = hybridInstruction.getDependencies();
        Set<Integer> necessaryLines = necessaryNodes.stream()
                .map(InstructionNode::getLine)
                .collect(HashSet::new, HashSet::add, HashSet::addAll);
        ArrayList<InstructionNode> necessaryNodesList = instructions.stream()
                .filter(x -> necessaryLines.contains(x.getLine()))
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        executableClassicalInstructions.addAll(necessaryNodesList.stream()
                .filter(x -> x.getLineType() == LineType.CLASSICAL)
                .filter(x -> !executableClassicalInstructions.contains(x))
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll));
        executableClassicalInstructions.addAll(necessaryNodesList.stream()
                .filter(x -> x.getLineType() != LineType.CLASSICAL)
                .filter(x -> !executableClassicalInstructions.contains(x))
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll));
        executableClassicalInstructions.add(hybridInstruction);

        executableClassicalInstructions.addAll(instructions.stream()
                .filter(x -> !executableClassicalInstructions.contains(x))
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll));
    }

    private ArrayList<InstructionNode> executableClassicalInstrutionsWithoutQuantum() {
        ExecutableInstructionsExtractor eie = new ExecutableInstructionsExtractor(new ArrayList<>(Collections.singletonList(instructions)));
        return eie.getExecutableInstructionsOfOneType(0, LineType.CLASSICAL, new ArrayList<>());
    }
}
