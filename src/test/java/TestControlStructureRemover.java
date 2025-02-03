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

import de.hhu.lirem101.quil_analyser.LineType;
import de.hhu.lirem101.quil_optimizer.InstructionNode;
import org.junit.jupiter.api.Test;

import static de.hhu.lirem101.quil_optimizer.ControlStructureRemover.removeControlStructures;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

class TestControlStructureRemover {

    @Test
    void removesControlStructuresCorrectly() {
        InstructionNode controlInstruction = new InstructionNode(1, LineType.CONTROL_STRUCTURE);
        InstructionNode normalInstruction = new InstructionNode(2, LineType.CLASSICAL);
        ArrayList<InstructionNode> innerInstructions = new ArrayList<>();
        innerInstructions.add(controlInstruction);
        innerInstructions.add(normalInstruction);
        ArrayList<ArrayList<InstructionNode>> instructions = new ArrayList<>(Collections.singletonList(innerInstructions));

        ArrayList<ArrayList<InstructionNode>> result = removeControlStructures(instructions);

        assertEquals(1, result.get(0).size());
        assertEquals(normalInstruction, result.get(0).get(0));
    }

    @Test
    void addsEndInstructionToEmptyBlocks() {
        InstructionNode controlInstruction = new InstructionNode(1, LineType.CONTROL_STRUCTURE);
        InstructionNode endInstruction = new InstructionNode(2, LineType.CONTROL_STRUCTURE);
        ArrayList<InstructionNode> innerInstructions = new ArrayList<>();
        innerInstructions.add(controlInstruction);
        innerInstructions.add(endInstruction);
        ArrayList<ArrayList<InstructionNode>> instructions = new ArrayList<>(Collections.singletonList(innerInstructions));

        ArrayList<ArrayList<InstructionNode>> result = removeControlStructures(instructions);

        assertEquals(1, result.get(0).size());
        assertEquals(endInstruction, result.get(0).get(0));
    }

    @Test
    void handlesMultipleInstructionBlocksCorrectly() {
        InstructionNode controlInstruction1 = new InstructionNode(0, LineType.CONTROL_STRUCTURE);
        InstructionNode normalInstruction1 = new InstructionNode(1, LineType.CLASSICAL);
        InstructionNode controlInstruction2 = new InstructionNode(2, LineType.CONTROL_STRUCTURE);
        InstructionNode normalInstruction2 = new InstructionNode(3, LineType.CLASSICAL);
        ArrayList<InstructionNode> innerInstructions1 = new ArrayList<>();
        innerInstructions1.add(controlInstruction1);
        innerInstructions1.add(normalInstruction1);
        ArrayList<InstructionNode> innerInstructions2 = new ArrayList<>();
        innerInstructions2.add(controlInstruction2);
        innerInstructions2.add(normalInstruction2);
        ArrayList<ArrayList<InstructionNode>> instructions = new ArrayList<>();
        instructions.add(innerInstructions1);
        instructions.add(innerInstructions2);

        ArrayList<ArrayList<InstructionNode>> result = removeControlStructures(instructions);

        assertEquals(2, result.size());
        assertEquals(1, result.get(0).size());
        assertEquals(normalInstruction1, result.get(0).get(0));
        assertEquals(1, result.get(1).size());
        assertEquals(normalInstruction2, result.get(1).get(0));
    }

    @Test
    void handlesEmptyInstructionListsCorrectly() {
        ArrayList<ArrayList<InstructionNode>> instructions = new ArrayList<>();

        ArrayList<ArrayList<InstructionNode>> result = removeControlStructures(instructions);

        assertTrue(result.isEmpty());
    }
}