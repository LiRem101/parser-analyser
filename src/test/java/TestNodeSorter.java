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

import com.kitfox.svg.A;
import de.hhu.lirem101.quil_optimizer.InstructionNode;
import de.hhu.lirem101.quil_optimizer.transformation.NodeSorter;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;

class TestNodeSorter {

    @Test
    void sortsNodesWithDependenciesCorrectly() {
        InstructionNode node1 = mock(InstructionNode.class);
        InstructionNode node2 = mock(InstructionNode.class);
        when(node1.getDependencies()).thenReturn(new HashSet<>());
        when(node2.getDependencies()).thenReturn(new HashSet<>(Collections.singletonList(node1)));
        ArrayList<InstructionNode> instructionList = new ArrayList<>();
        instructionList.add(node1);
        instructionList.add(node2);

        ArrayList<InstructionNode> sortedList = NodeSorter.sortNodes(instructionList);

        assertEquals(2, sortedList.size());
        assertEquals(node1, sortedList.get(0));
        assertEquals(node2, sortedList.get(1));
    }

    @Test
    void handlesEmptyInstructionListCorrectly() {
        ArrayList<InstructionNode> instructionList = new ArrayList<>();

        ArrayList<InstructionNode> sortedList = NodeSorter.sortNodes(instructionList);

        assertTrue(sortedList.isEmpty());
    }

    @Test
    void handlesSingleNodeListCorrectly() {
        InstructionNode node = mock(InstructionNode.class);
        when(node.getDependencies()).thenReturn(new HashSet<>());
        ArrayList<InstructionNode> instructionList = new ArrayList<>(Collections.singletonList(node));

        ArrayList<InstructionNode> sortedList = NodeSorter.sortNodes(instructionList);

        assertEquals(1, sortedList.size());
        assertEquals(node, sortedList.get(0));
    }

    @Test
    void handlesMultipleIndependentNodesCorrectly() {
        InstructionNode node1 = mock(InstructionNode.class);
        InstructionNode node2 = mock(InstructionNode.class);
        when(node1.getDependencies()).thenReturn(new HashSet<>());
        when(node2.getDependencies()).thenReturn(new HashSet<>());
        ArrayList<InstructionNode> instructionList = new ArrayList<>();
        instructionList.add(node1);
        instructionList.add(node2);

        ArrayList<InstructionNode> sortedList = NodeSorter.sortNodes(instructionList);

        assertEquals(2, sortedList.size());
        assertTrue(sortedList.contains(node1));
        assertTrue(sortedList.contains(node2));
    }
}