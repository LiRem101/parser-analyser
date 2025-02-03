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

import de.hhu.lirem101.quil_analyser.LineType;
import de.hhu.lirem101.quil_optimizer.InstructionNode;
import de.hhu.lirem101.quil_optimizer.SortNodesIntoInstructions;
import org.junit.jupiter.api.Test;
import org.snt.inmemantlr.tree.ParseTreeNode;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TestSortNodesIntoInstructions {

    @Test
    void appendsNodeToEmptyInstructions() {
        Map<Integer, ParseTreeNode> sortedNodes = new HashMap<>();
        sortedNodes.put(1, mock(ParseTreeNode.class));
        SortNodesIntoInstructions sorter = new SortNodesIntoInstructions(sortedNodes);
        ArrayList<ArrayList<InstructionNode>> instructions = new ArrayList<>();

        sorter.appendNodeToInstructions(instructions);

        assertTrue(instructions.isEmpty());
    }

    @Test
    void appendsNodeToSingleInstruction() {
        ParseTreeNode node = mock(ParseTreeNode.class);
        when(node.getRule()).thenReturn("instr");
        Map<Integer, ParseTreeNode> sortedNodes = new HashMap<>();
        sortedNodes.put(1, node);
        SortNodesIntoInstructions sorter = new SortNodesIntoInstructions(sortedNodes);
        InstructionNode instrNode = new InstructionNode(1, LineType.CLASSICAL);
        ArrayList<InstructionNode> innerList = new ArrayList<>();
        innerList.add(instrNode);
        ArrayList<ArrayList<InstructionNode>> instructions = new ArrayList<>();
        instructions.add(innerList);

        sorter.appendNodeToInstructions(instructions);

        assertEquals(node, instrNode.getParseTreeNode());
    }

    @Test
    void appendsNodeToMultipleInstructions() {
        ParseTreeNode node = mock(ParseTreeNode.class);
        when(node.getRule()).thenReturn("instr");
        Map<Integer, ParseTreeNode> sortedNodes = new HashMap<>();
        sortedNodes.put(1, node);
        SortNodesIntoInstructions sorter = new SortNodesIntoInstructions(sortedNodes);
        InstructionNode instrNode1 = new InstructionNode(1, LineType.CLASSICAL);
        InstructionNode instrNode2 = new InstructionNode(1, LineType.QUANTUM);
        ArrayList<InstructionNode> innerList1 = new ArrayList<>();
        innerList1.add(instrNode1);
        ArrayList<InstructionNode> innerList2 = new ArrayList<>();
        innerList1.add(instrNode2);
        ArrayList<ArrayList<InstructionNode>> instructions = new ArrayList<>();
        instructions.add(innerList1);
        instructions.add(innerList2);

        sorter.appendNodeToInstructions(instructions);

        assertEquals(node, instrNode1.getParseTreeNode());
        assertEquals(node, instrNode2.getParseTreeNode());
    }
}