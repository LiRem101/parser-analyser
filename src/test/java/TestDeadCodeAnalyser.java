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
import de.hhu.lirem101.quil_optimizer.analysis.DeadCodeAnalyser;
import org.junit.jupiter.api.Test;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TestDeadCodeAnalyser {

    @Test
    void returnsEmptyIndizesWhenNoInstructions() {
        ArrayList<ArrayList<InstructionNode>> instructions = new ArrayList<>();
        ArrayList<Set<Integer>> indexToJumpTo = new ArrayList<>();
        DeadCodeAnalyser analyser = new DeadCodeAnalyser(instructions, indexToJumpTo);

        Set<Integer> result = analyser.getIndizesOfDeadLines();

        assertTrue(result.isEmpty());
    }

    @Test
    void returnsEmptyDeadLinesWhenNoInstructions() {
        ArrayList<ArrayList<InstructionNode>> instructions = new ArrayList<>();
        ArrayList<Set<Integer>> indexToJumpTo = new ArrayList<>();
        DeadCodeAnalyser analyser = new DeadCodeAnalyser(instructions, indexToJumpTo);

        ArrayList<Set<Integer>> result = analyser.getDeadLines();

        assertTrue(result.isEmpty());
    }

    @Test
    void calculatesDeadLinesCorrectly() {
        InstructionNode instruction = mock(InstructionNode.class);
        when(instruction.getLine()).thenReturn(1);
        when(instruction.getShownToBeDead()).thenReturn(true);
        ArrayList<ArrayList<InstructionNode>> instructions = new ArrayList<>(Collections.singletonList(new ArrayList<>(Collections.singletonList(instruction))));
        ArrayList<Set<Integer>> indexToJumpTo = new ArrayList<>(Collections.singletonList(new HashSet<>()));
        DeadCodeAnalyser analyser = new DeadCodeAnalyser(instructions, indexToJumpTo);

        ArrayList<Set<Integer>> result = analyser.getDeadLines();

        assertEquals(1, result.size());
        assertTrue(result.get(0).contains(1));
    }

    @Test
    void calculatesIndizesOfDeadLinesCorrectlyWithDeadBlock() {
        InstructionNode instruction1 = mock(InstructionNode.class);
        InstructionNode instrcution2 = mock(InstructionNode.class);
        when(instruction1.getLine()).thenReturn(1);
        when(instruction1.getShownToBeDead()).thenReturn(false);
        when(instrcution2.getLine()).thenReturn(2);
        when(instrcution2.getShownToBeDead()).thenReturn(false);
        ArrayList<ArrayList<InstructionNode>> instructions = new ArrayList<>();
        instructions.add(new ArrayList<>(Collections.singletonList(instruction1)));
        instructions.add(new ArrayList<>(Collections.singletonList(instrcution2)));
        ArrayList<Set<Integer>> indexToJumpTo = new ArrayList<>(Collections.singletonList(new HashSet<>()));
        DeadCodeAnalyser analyser = new DeadCodeAnalyser(instructions, indexToJumpTo);

        Set<Integer> result = analyser.getIndizesOfDeadLines();

        assertEquals(1, result.size());
        assertTrue(result.contains(1));
    }

    @Test
    void calculatesIndizesOfDeadLinesCorrectlyWithoutDeadBlock() {
        InstructionNode instruction1 = mock(InstructionNode.class);
        InstructionNode instrcution2 = mock(InstructionNode.class);
        when(instruction1.getLine()).thenReturn(1);
        when(instruction1.getShownToBeDead()).thenReturn(false);
        when(instrcution2.getLine()).thenReturn(2);
        when(instrcution2.getShownToBeDead()).thenReturn(false);
        ArrayList<ArrayList<InstructionNode>> instructions = new ArrayList<>();
        instructions.add(new ArrayList<>(Collections.singletonList(instruction1)));
        instructions.add(new ArrayList<>(Collections.singletonList(instrcution2)));
        ArrayList<Set<Integer>> indexToJumpTo = new ArrayList<>(Collections.singletonList(new HashSet<>(Collections.singletonList(1))));
        DeadCodeAnalyser analyser = new DeadCodeAnalyser(instructions, indexToJumpTo);

        Set<Integer> result = analyser.getIndizesOfDeadLines();

        assertTrue(result.isEmpty());
    }
}