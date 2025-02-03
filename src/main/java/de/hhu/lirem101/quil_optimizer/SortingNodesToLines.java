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

import org.snt.inmemantlr.tree.ParseTreeNode;

import java.util.*;

/**
 * Class that finds the root node for every line ine the parse tree and sorts it into a map with the line number as key.
 */
public class SortingNodesToLines {
    private final Set<String> instructionStarters = Collections.unmodifiableSet(new HashSet<>(Arrays.asList("instr", "circuitInstr")));

    private final HashMap<Integer, ParseTreeNode> sortedNodes = new HashMap<>();
    private final ParseTreeNode node;
    private boolean calculated = false;

    public SortingNodesToLines(ParseTreeNode node) {
        this.node = node;
    }

    /**
     * Returns a map with the line number as key and the root node of the line as value.
     * @return The map with the line number as key and the root node of the line as value.
     */
    public HashMap<Integer, ParseTreeNode> getSortedNodes() {
        if (!calculated) {
            calculateSortedNodes();
            calculated = true;
        }
        return new HashMap<>(sortedNodes);
    }

    /**
     * Calculates a map with the line number as key and the root node of the line as value.
     * Saves the mao into this.sortedNodes.
     */
    private void calculateSortedNodes() {
        LinkedList<ParseTreeNode> nodeQueue = new LinkedList<>();
        nodeQueue.add(node);
        ParseTreeNode currentNode = nodeQueue.poll();
        while (currentNode != null) {
            int line = currentNode.getLine();
            if (instructionStarters.contains(currentNode.getRule())) {
                sortedNodes.put(line, currentNode.getFirstChild());
            } else {
                nodeQueue.addAll(currentNode.getChildren());
            }
            currentNode = nodeQueue.poll();
        }
    }

}
