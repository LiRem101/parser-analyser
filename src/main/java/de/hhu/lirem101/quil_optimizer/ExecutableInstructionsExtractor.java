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

package de.hhu.lirem101.quil_optimizer;

import de.hhu.lirem101.quil_analyser.LineType;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class ExecutableInstructionsExtractor {
    private final ArrayList<ArrayList<InstructionNode>> instructions;

    public ExecutableInstructionsExtractor(ArrayList<ArrayList<InstructionNode>> instructions) {
        this.instructions = instructions;
    }

    /**
     * Get all instructions of which all previous instructions are already in the execution queue.
     * @param executionQueue The list of list of instructions that are already in the execution queue.
     * @return A list of list of instructions whose previous instructions are already in the execution queue.
     */
    public ArrayList<ArrayList<InstructionNode>> getExecutableInstructions(ArrayList<ArrayList<InstructionNode>> executionQueue) {
        ArrayList<ArrayList<InstructionNode>> executableInstructionsList = new ArrayList<>();
        if(this.instructions.size() != executionQueue.size()) {
            throw new IllegalArgumentException("The size of the instructions list and the executableInstructions list" +
                    " must be the same.");
        }
        for(int i = 0; i < instructions.size(); i++) {
            executableInstructionsList.add(getExecutableInstructionsOfOneBlock(i, executionQueue.get(i)));
        }
        return executableInstructionsList;
    }

    /**
     * Get the executable instructions for a given blockIndex.
     * @param blockIndex The blockIndex of the instructions to look at.
     * @param executionQueue The list of instrcutions that are already in the execution queue.
     * @return A list of instructions whose previous instructions are already in the execution queue.
     */
    public ArrayList<InstructionNode> getExecutableInstructionsOfOneBlock(int blockIndex, ArrayList<InstructionNode> executionQueue) {
        ArrayList<InstructionNode> instructionList = instructions.get(blockIndex);
        ArrayList<InstructionNode> executableInstructions = new ArrayList<>();
        ArrayList<Integer> executedLines = executionQueue.stream().map(InstructionNode::getLine).collect(Collectors.toCollection(ArrayList::new));
        for(InstructionNode node : instructionList) {
            ArrayList<Integer> necessaryLines = node.getBranches().stream().map(InstructionNode::getLine).collect(Collectors.toCollection(ArrayList::new));
            if(!executionQueue.contains(node) && executedLines.containsAll(necessaryLines)) {
                executableInstructions.add(node);
                executedLines.add(node.getLine());
            }
        }
        return executableInstructions;
    }

    /**
     * Get the executable instructions for a given index if only a specific type of instruction can be executed.
     * @param index The index of the instructions to look at.
     * @param type The type of the instructions to consider.
     * @param executionQueue The list of instrcutions that are already in the execution queue.
     * @return A list of instructions whose previous instructions are already in the execution queue.
     */
    public ArrayList<InstructionNode> getExecutableInstructionsOfOneType(int index, LineType type, ArrayList<InstructionNode> executionQueue) {
        ArrayList<InstructionNode> instructionList = instructions.get(index);
        ArrayList<InstructionNode> executableInstructions = new ArrayList<>();
        ArrayList<Integer> executedLines = executionQueue.stream().map(InstructionNode::getLine).collect(Collectors.toCollection(ArrayList::new));
        for(InstructionNode node : instructionList) {
            if(node.getLineType() == type) {
                ArrayList<Integer> necessaryLines = node.getBranches().stream().map(InstructionNode::getLine).collect(Collectors.toCollection(ArrayList::new));
                if (!executionQueue.contains(node) && executedLines.containsAll(necessaryLines)) {
                    executableInstructions.add(node);
                    executedLines.add(node.getLine());
                }
            }
        }
        return executableInstructions;
    }
}
