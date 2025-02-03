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
import java.util.stream.IntStream;

public class ControlStructureRemover {

    /**
     * Removes all non-conditional control structures from the instructions. If we result in an empty block, we add an
     * end instruction to the block. An empty block can only occur in a list that leads to program halt.
     * @param instructionLists The instructions to remove the control structures from.
     * @return List of lists of instructions with removed unconditional control structures.
     */
    public static ArrayList<ArrayList<InstructionNode>> removeControlStructures(ArrayList<ArrayList<InstructionNode>> instructionLists) {
        ArrayList<ArrayList<InstructionNode>> removedInstructions = new ArrayList<>();
        for (ArrayList<InstructionNode> instructionList : instructionLists) {
            ArrayList<InstructionNode> theseRemovedInstructions = instructionList.stream()
                    .filter(instruction -> instruction.getLineType() != LineType.CONTROL_STRUCTURE)
                    .collect(Collectors.toCollection(ArrayList::new));
            removedInstructions.add(theseRemovedInstructions);
        }
        ArrayList<Integer> indizesOfEmptyBlocks = IntStream.range(0, removedInstructions.size())
                .filter(i -> removedInstructions.get(i).isEmpty())
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        for (Integer index : indizesOfEmptyBlocks) {
            int size = instructionLists.get(index).size();
            InstructionNode endInstruction = instructionLists.get(index).get(size-1);
            removedInstructions.get(index).add(endInstruction);
        }
        return removedInstructions;
    }
}
