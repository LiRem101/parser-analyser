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

import de.hhu.lirem101.quil_analyser.ControlFlowBlock;
import de.hhu.lirem101.quil_analyser.LineType;
import de.hhu.lirem101.quil_optimizer.InstructionListCreator;
import de.hhu.lirem101.quil_optimizer.InstructionNode;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.stream.Collectors;

class TestInstructionListCreator {

    @Test
    void returnsEmptyInstructionsWhenNoCodelines() {
        ControlFlowBlock block = new ControlFlowBlock("test");
        Map<Integer, LineType> classes = new HashMap<>();
        InstructionListCreator creator = new InstructionListCreator(block, classes);

        ArrayList<ArrayList<InstructionNode>> result = creator.getInstructions();

        assertTrue(result.isEmpty());
    }

    @Test
    void calculatesInstructionsForSingleBlock() {
        ControlFlowBlock block = new ControlFlowBlock("test");
        block.addCodeline(1);
        Map<Integer, LineType> classes = new HashMap<>();
        classes.put(1, LineType.CLASSICAL);
        InstructionListCreator creator = new InstructionListCreator(block, classes);

        ArrayList<ArrayList<InstructionNode>> result = creator.getInstructions();

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).size());
        assertEquals(1, result.get(0).get(0).getLine());
    }

    @Test
    void calculatesInstructionsForMultipleBlocks() {
        ControlFlowBlock blockControl1 = new ControlFlowBlock("testControl1");
        blockControl1.addCodeline(1);
        ControlFlowBlock blockQuantum = new ControlFlowBlock("testQuantum");
        blockQuantum.addCodeline(2);
        ControlFlowBlock blockClassical = new ControlFlowBlock("testClassical");
        blockClassical.addCodeline(3);
        blockControl1.addBranch(blockClassical);
        blockControl1.addBranch(blockQuantum);
        ControlFlowBlock blockControl2 = new ControlFlowBlock("testControl2");
        blockControl1.addCodeline(4);
        blockQuantum.addBranch(blockControl2);
        blockClassical.addBranch(blockControl2);
        Map<Integer, LineType> classes = new HashMap<>();
        classes.put(1, LineType.CONTROL_STRUCTURE);
        classes.put(2, LineType.QUANTUM);
        classes.put(3, LineType.CLASSICAL);
        classes.put(4, LineType.CONTROL_STRUCTURE);

        InstructionListCreator creator = new InstructionListCreator(blockControl1, classes);

        ArrayList<ArrayList<InstructionNode>> result = creator.getInstructions();

        assertEquals(1, result.size());
        ArrayList<InstructionNode> instructions = result.get(0);
        assertEquals(4, instructions.size());
        assertEquals(1, instructions.get(0).getLine());
        assertEquals(2, instructions.get(1).getLine());
        assertEquals(3, instructions.get(2).getLine());
        assertEquals(4, instructions.get(3).getLine());
        assertEquals(LineType.CONTROL_STRUCTURE, instructions.get(0).getLineType());
        assertEquals(LineType.QUANTUM, instructions.get(1).getLineType());
        assertEquals(LineType.CLASSICAL, instructions.get(2).getLineType());
        assertEquals(LineType.CONTROL_STRUCTURE, instructions.get(3).getLineType());
    }

    @Test
    void handlesConditionalJumpsCorrectly() {
        ControlFlowBlock blockControl = new ControlFlowBlock("testControl");
        blockControl.addCodeline(1);
        ControlFlowBlock blockQuantum = new ControlFlowBlock("testQuantum");
        blockQuantum.addCodeline(2);
        ControlFlowBlock blockClassical = new ControlFlowBlock("testClassical");
        blockClassical.addCodeline(3);
        blockControl.addBranch(blockQuantum);
        blockControl.addBranch(blockClassical);
        Map<Integer, LineType> classes = new HashMap<>();
        classes.put(1, LineType.CONTROL_STRUCTURE_INFLUENCED_CLASSICAL);
        classes.put(2, LineType.QUANTUM);
        classes.put(3, LineType.CLASSICAL);
        InstructionListCreator creator = new InstructionListCreator(blockControl, classes);

        ArrayList<ArrayList<InstructionNode>> result = creator.getInstructions();

        assertEquals(3, result.size());
        ArrayList<InstructionNode> instructionsControl = result.get(0);
        ArrayList<InstructionNode> instructionsQuantum = result.stream()
                .filter(x -> x.get(0).getLineType() == LineType.QUANTUM)
                .findFirst()
                .orElse(null);
        ArrayList<InstructionNode> instructionsClassical = result.stream()
                .filter(x -> x.get(0).getLineType() == LineType.CLASSICAL)
                .findFirst()
                .orElse(null);

        assertEquals(1, instructionsControl.get(0).getLine());
        assertEquals(LineType.CONTROL_STRUCTURE_INFLUENCED_CLASSICAL, instructionsControl.get(0).getLineType());
        assertEquals(2, instructionsQuantum.get(0).getLine());
        assertEquals(LineType.QUANTUM, instructionsQuantum.get(0).getLineType());
        assertEquals(3, instructionsClassical.get(0).getLine());
        assertEquals(LineType.CLASSICAL, instructionsClassical.get(0).getLineType());
    }

    @Test
    void handlesLoops() {
        ControlFlowBlock blockControl1 = new ControlFlowBlock("testControl1");
        blockControl1.addCodeline(1);
        ControlFlowBlock blockQuantum = new ControlFlowBlock("testQuantum");
        blockQuantum.addCodeline(2);
        ControlFlowBlock blockControl2 = new ControlFlowBlock("testControl2");
        blockControl2.addCodeline(3);
        blockControl2.addCodeline(4);
        ControlFlowBlock blockClassical = new ControlFlowBlock("testClassical");
        blockClassical.addCodeline(5);
        blockControl1.addBranch(blockQuantum);
        blockQuantum.addBranch(blockControl2);
        blockControl2.addBranch(blockClassical);
        blockControl2.addBranch(blockControl1);

        Map<Integer, LineType> classes = new HashMap<>();
        classes.put(1, LineType.CONTROL_STRUCTURE);
        classes.put(2, LineType.QUANTUM);
        classes.put(3, LineType.CLASSICAL_INFLUENCES_QUANTUM);
        classes.put(4, LineType.CONTROL_STRUCTURE_INFLUENCED_CLASSICAL);
        classes.put(5, LineType.CLASSICAL);

        InstructionListCreator creator = new InstructionListCreator(blockControl1, classes);
        ArrayList<ArrayList<InstructionNode>> result = creator.getInstructions();

        assertEquals(2, result.size());
        ArrayList<InstructionNode> fourInstruction = result
                .stream()
                .filter(x -> x.size() == 4)
                .findFirst()
                .orElse(null);
        ArrayList<InstructionNode> oneInstruction = result
                .stream()
                .filter(x -> x.size() == 1)
                .findFirst()
                .orElse(null);

        assertNotNull(fourInstruction);
        assertNotNull(oneInstruction);
    }
}