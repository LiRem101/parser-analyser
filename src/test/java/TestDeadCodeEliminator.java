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

import de.hhu.lirem101.quil_optimizer.InstructionNode;
import de.hhu.lirem101.quil_optimizer.transformation.DeadCodeEliminator;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;

class TestDeadCodeEliminator {

    @Test
    void eliminatesDeadCodeCorrectly() {
        InstructionNode node1 = mock(InstructionNode.class);
        InstructionNode node2 = mock(InstructionNode.class);
        when(node1.getLine()).thenReturn(1);
        when(node2.getLine()).thenReturn(2);
        ArrayList<InstructionNode> list = new ArrayList<>();
        list.add(node1);
        list.add(node2);
        ArrayList<ArrayList<InstructionNode>> instructions = new ArrayList<>(Collections.singletonList(list));
        Set<Integer> deadLinesSet = new HashSet<>();
        deadLinesSet.add(1);
        ArrayList<Set<Integer>> deadLines = new ArrayList<>(Collections.singletonList(deadLinesSet));
        Set<Integer> indizesOfDeadLineBlocks = new HashSet<>();
        indizesOfDeadLineBlocks.add(0);
        DeadCodeEliminator eliminator = new DeadCodeEliminator(instructions, deadLines, indizesOfDeadLineBlocks);

        eliminator.eliminateDeadCode();

        assertEquals(1, instructions.size());
        assertTrue(instructions.get(0).isEmpty());
    }

    @Test
    void handlesEmptyInstructionsCorrectly() {
        ArrayList<ArrayList<InstructionNode>> instructions = new ArrayList<>();
        ArrayList<Set<Integer>> deadLines = new ArrayList<>();
        Set<Integer> indizesOfDeadLineBlocks = new HashSet<>();
        DeadCodeEliminator eliminator = new DeadCodeEliminator(instructions, deadLines, indizesOfDeadLineBlocks);

        eliminator.eliminateDeadCode();

        assertTrue(instructions.isEmpty());
    }

    @Test
    void handlesNoDeadLinesCorrectly() {
        InstructionNode node1 = mock(InstructionNode.class);
        InstructionNode node2 = mock(InstructionNode.class);
        when(node1.getLine()).thenReturn(1);
        when(node2.getLine()).thenReturn(2);
        ArrayList<InstructionNode> list = new ArrayList<>();
        list.add(node1);
        list.add(node2);
        ArrayList<ArrayList<InstructionNode>> instructions = new ArrayList<>(Collections.singletonList(list));
        ArrayList<Set<Integer>> deadLines = new ArrayList<>(Collections.singletonList(new HashSet<>()));
        Set<Integer> indizesOfDeadLineBlocks = new HashSet<>();
        DeadCodeEliminator eliminator = new DeadCodeEliminator(instructions, deadLines, indizesOfDeadLineBlocks);

        eliminator.eliminateDeadCode();

        assertEquals(1, instructions.size());
        assertEquals(2, instructions.get(0).size());
    }

    @Test
    void handlesNoDeadBlocksCorrectly() {
        InstructionNode node1 = mock(InstructionNode.class);
        InstructionNode node2 = mock(InstructionNode.class);
        when(node1.getLine()).thenReturn(1);
        when(node2.getLine()).thenReturn(2);
        ArrayList<InstructionNode> list = new ArrayList<>();
        list.add(node1);
        list.add(node2);
        ArrayList<ArrayList<InstructionNode>> instructions = new ArrayList<>(Collections.singletonList(list));
        Set<Integer> deadLinesSet = new HashSet<>();
        deadLinesSet.add(1);
        ArrayList<Set<Integer>> deadLines = new ArrayList<>(Collections.singletonList(deadLinesSet));
        Set<Integer> indizesOfDeadLineBlocks = new HashSet<>();
        DeadCodeEliminator eliminator = new DeadCodeEliminator(instructions, deadLines, indizesOfDeadLineBlocks);

        eliminator.eliminateDeadCode();

        assertEquals(1, instructions.get(0).size());
        assertEquals(node2, instructions.get(0).get(0));
    }
}