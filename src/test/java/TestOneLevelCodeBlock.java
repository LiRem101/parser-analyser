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

import de.hhu.lirem101.quil_analyser.OneLevelCodeBlock;
import org.junit.jupiter.api.Test;
import org.snt.inmemantlr.tree.ParseTreeNode;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TestOneLevelCodeBlock {

    @Test
    void constructsWithValidNode() {
        ParseTreeNode node = mock(ParseTreeNode.class);
        ParseTreeNode childNode = mock(ParseTreeNode.class);
        when(node.getRule()).thenReturn("defLabel");
        when(node.getLine()).thenReturn(1);
        when(node.getFirstChild()).thenReturn(childNode);
        when(childNode.getLabel()).thenReturn("label1");
        OneLevelCodeBlock block = new OneLevelCodeBlock(node);
        assertTrue(block.getLabels().containsValue("label1"));
        assertEquals("label1", block.getLabels().get(1));
    }

    @Test
    void processesDefCircuit() {
        ParseTreeNode node = mock(ParseTreeNode.class);
        ParseTreeNode childNode = mock(ParseTreeNode.class);
        ParseTreeNode childNode2 = mock(ParseTreeNode.class);
        when(node.getRule()).thenReturn("defCircuit");
        when(node.getLine()).thenReturn(1);
        when(node.getFirstChild()).thenReturn(childNode);
        when(childNode.getLabel()).thenReturn("circuit1");
        when(childNode.getLastChild()).thenReturn(childNode2);
        when(childNode2.getRule()).thenReturn("gate");
        when(childNode2.getChildren()).thenReturn(new ArrayList<>());
        when(childNode2.getLine()).thenReturn(2);
        OneLevelCodeBlock block = new OneLevelCodeBlock(node);
        assertTrue(block.getCircuitsNextLevel().containsKey("circuit1"));
        assertEquals("circuit1", block.getLinesCircuitsNextLevel().get(1));
    }

    @Test
    void callDefCircuit() {
        ParseTreeNode parent = mock(ParseTreeNode.class);
        ParseTreeNode node = mock(ParseTreeNode.class);
        ParseTreeNode childNode = mock(ParseTreeNode.class);
        ParseTreeNode childNode2 = mock(ParseTreeNode.class);
        ParseTreeNode callCircuitNode = mock(ParseTreeNode.class);
        ParseTreeNode callCircuitChildNode = mock(ParseTreeNode.class);
        ArrayList<ParseTreeNode> parentsChildren = new ArrayList<>();
        parentsChildren.add(node);
        parentsChildren.add(callCircuitNode);
        ArrayList<ParseTreeNode> callNodeChildren = new ArrayList<>();
        callNodeChildren.add(callCircuitChildNode);
        when(parent.getRule()).thenReturn("root");
        when(parent.getLine()).thenReturn(1);
        when(parent.getChildren()).thenReturn(parentsChildren);
        when(node.getRule()).thenReturn("defCircuit");
        when(node.getLine()).thenReturn(1);
        when(node.getFirstChild()).thenReturn(childNode);
        when(childNode.getLabel()).thenReturn("circuit1");
        when(childNode.getLastChild()).thenReturn(childNode2);
        when(childNode2.getRule()).thenReturn("gate");
        when(childNode2.getChildren()).thenReturn(new ArrayList<>());
        when(childNode2.getLine()).thenReturn(2);
        when(callCircuitNode.getRule()).thenReturn("gate");
        when(callCircuitNode.getLine()).thenReturn(3);
        when(callCircuitNode.getChildren()).thenReturn(callNodeChildren);
        when(callCircuitNode.getFirstChild()).thenReturn(callCircuitChildNode);
        when(callCircuitChildNode.getLabel()).thenReturn("circuit1");
        when(callCircuitChildNode.getRule()).thenReturn("name");
        when(callCircuitChildNode.getChildren()).thenReturn(new ArrayList<>());
        when(callCircuitChildNode.getLine()).thenReturn(3);

        OneLevelCodeBlock block = new OneLevelCodeBlock(parent);
        assertTrue(block.getJumpToCircuits().containsValue("circuit1"));
        assertEquals("circuit1", block.getJumpToCircuits().get(3));
    }

    @Test
    void processesDefGate() {
        ParseTreeNode node = mock(ParseTreeNode.class);
        ParseTreeNode childNode = mock(ParseTreeNode.class);
        ParseTreeNode childNode2 = mock(ParseTreeNode.class);
        when(node.getRule()).thenReturn("defGate");
        when(node.getLine()).thenReturn(1);
        when(node.getFirstChild()).thenReturn(childNode);
        when(childNode.getLabel()).thenReturn("gate1");
        when(childNode.getLastChild()).thenReturn(childNode2);
        when(childNode2.getRule()).thenReturn("gate");
        when(childNode2.getChildren()).thenReturn(new ArrayList<>());
        when(childNode2.getLine()).thenReturn(2);
        OneLevelCodeBlock block = new OneLevelCodeBlock(node);
        assertTrue(block.getDefinedGates().contains("gate1"));
    }

    @Test
    void processesJumpInstructions() {
        ParseTreeNode node = mock(ParseTreeNode.class);
        ParseTreeNode childNode = mock(ParseTreeNode.class);
        ParseTreeNode childNode2 = mock(ParseTreeNode.class);
        ArrayList<ParseTreeNode> children = new ArrayList<>();
        children.add(childNode);
        children.add(childNode2);
        when(node.getRule()).thenReturn("jump");
        when(node.getLine()).thenReturn(1);
        when(node.getFirstChild()).thenReturn(childNode);
        when(node.getChildren()).thenReturn(children);
        when(childNode.getLabel()).thenReturn("label1");
        when(childNode.getRule()).thenReturn("label");
        when(childNode.getLastChild()).thenReturn(childNode2);
        when(childNode2.getRule()).thenReturn("addr");
        when(childNode2.getLine()).thenReturn(1);
        OneLevelCodeBlock block = new OneLevelCodeBlock(node);
        assertTrue(block.getJumpsSameLevel().containsValue("label1"));
    }

    @Test
    void processesJumpUnlessInstructions() {
        ParseTreeNode node = mock(ParseTreeNode.class);
        ParseTreeNode childNode = mock(ParseTreeNode.class);
        ParseTreeNode childNode2 = mock(ParseTreeNode.class);
        ArrayList<ParseTreeNode> children = new ArrayList<>();
        children.add(childNode);
        children.add(childNode2);
        when(node.getRule()).thenReturn("jumpUnless");
        when(node.getLine()).thenReturn(1);
        when(node.getFirstChild()).thenReturn(childNode);
        when(node.getChildren()).thenReturn(children);
        when(childNode.getLabel()).thenReturn("label1");
        when(childNode.getRule()).thenReturn("label");
        when(childNode.getLastChild()).thenReturn(childNode2);
        when(childNode2.getRule()).thenReturn("addr");
        when(childNode2.getLine()).thenReturn(1);
        OneLevelCodeBlock block = new OneLevelCodeBlock(node);
        assertTrue(block.getJumpsCondSameLevel().containsValue("label1"));
    }

    @Test
    void handlesEmptyNode() {
        OneLevelCodeBlock block = new OneLevelCodeBlock(null);
        assertTrue(block.getValidCodelines().isEmpty());
    }
}