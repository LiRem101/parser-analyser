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

import de.hhu.lirem101.quil_optimizer.ExecutableInstructionsExtractor;
import de.hhu.lirem101.quil_optimizer.InstructionNode;

import java.util.ArrayList;
import java.util.Collections;

public class NodeSorter {

    /**
     * Sorts the execution order of the nodes in the instruction list. Ensures that no node is executed before all its
     * dependencies are executed.
     * @param instructionList The list of instructions to sort.
     * @return The sorted list of instructions.
     */
    public static ArrayList<InstructionNode> sortNodes(ArrayList<InstructionNode> instructionList) {
        ArrayList<InstructionNode> newOrder = new ArrayList<>();
        sortNodesWithGivenExecutables(instructionList, newOrder);
        return newOrder;
    }

    /**
     * Sorts nodes from instructionList into executionOrder.
     * @param instructionList The list of instructions to sort.
     * @param executionOrder The list of instructions that are already in the execution queue.
     */
    public static void sortNodesWithGivenExecutables(ArrayList<InstructionNode> instructionList, ArrayList<InstructionNode> executionOrder) {
        boolean newNodes = true;
        instructionList.removeAll(executionOrder);
        while (!executionOrder.containsAll(instructionList) && newNodes && !instructionList.isEmpty()) {
            ExecutableInstructionsExtractor executableExtractor = new ExecutableInstructionsExtractor(new ArrayList<>(Collections.singletonList(instructionList)));
            ArrayList<InstructionNode> executable = executableExtractor.getExecutableInstructionsOfOneBlock(0, executionOrder);
            newNodes = !executable.isEmpty();
            executionOrder.addAll(executable);
            instructionList.removeAll(executable);
        }
    }

}
