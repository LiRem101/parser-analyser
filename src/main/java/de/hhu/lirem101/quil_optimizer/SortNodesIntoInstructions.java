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

package de.hhu.lirem101.quil_optimizer;

import org.snt.inmemantlr.tree.ParseTreeNode;

import java.util.ArrayList;
import java.util.Map;

public class SortNodesIntoInstructions {
    private final Map<Integer, ParseTreeNode> sortedNodes;
    //private final ArrayList<ArrayList<InstructionNode>> instructions;

    public SortNodesIntoInstructions(Map<Integer, ParseTreeNode> nodes) {
        this.sortedNodes = nodes;
    }

    /**
     * Appends the node of the right line to the given instructions. A node may be appended to multiple instructions.
     * @param instructions The list of lists of instructions to append the node to.
     */
    public void appendNodeToInstructions(ArrayList<ArrayList<InstructionNode>> instructions) {
        for(ArrayList<InstructionNode> list : instructions) {
            for(InstructionNode node : list) {
                node.setParseTreeNode(sortedNodes.get(node.getLine()));
            }
        }
    }
}
