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

package de.hhu.lirem101.quil_optimizer.transformation.constant_folding;

import de.hhu.lirem101.quil_optimizer.quil_variable.ClassicalVariable;
import org.snt.inmemantlr.tree.ParseTree;
import org.snt.inmemantlr.tree.ParseTreeNode;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class ParseTreeCreator {

    /**
     * Create a ParseTree node to MOVE the value to the variable.
     * @param variable The variable to move the value to.
     * @param value The value to move.
     * @param pt The ParseTree node to replace.
     * @return The ParseTree node.
     */
    public static ParseTreeNode createMoveInstruction(ClassicalVariable variable, double value, ParseTreeNode pt) {
        ParseTree tree = new ParseTree("MOVE", "MOVE");
        String label = "MOVE" + variable + Double.toString(value);
        ParseTreeNode node = tree.newNode(pt.getParent(), "classicalBinary", label, pt.getSidx(), pt.getEidx(), pt.getLine(), pt.getCharPositionInLine());
        ParseTreeNode moveNode = tree.newNode(node, "move", label, pt.getSidx(), pt.getEidx(), pt.getLine(), pt.getCharPositionInLine());
        node.addChild(moveNode);
        ParseTreeNode paramNode = tree.newNode(moveNode, "addr", variable.getName(), pt.getSidx(), pt.getEidx(), pt.getLine(), 5);
        moveNode.addChild(paramNode);
        ParseTreeNode valueNode = tree.newNode(moveNode, "number", Double.toString(value), pt.getSidx(), pt.getEidx(), pt.getLine(), 5 + variable.getName().length());
        moveNode.addChild(valueNode);
        ParseTreeNode realNNode = tree.newNode(valueNode, "realN", Double.toString(value), pt.getSidx(), pt.getEidx(), pt.getLine(), valueNode.getCharPositionInLine());
        valueNode.addChild(realNNode);
        return node;
    }

    public static void replaceVariableByConstant(ParseTreeNode pt, String variable, double value) {
        ParseTreeNode valNode = findValNode(pt, variable);
        if (valNode == null) {
            return;
        }
        ParseTreeNode parent = valNode.getParent();
        ParseTree tree = new ParseTree("", "");
        ParseTreeNode valueNode = tree.newNode(parent, "number", Double.toString(value), valNode.getSidx(), valNode.getEidx(), valNode.getLine(), valNode.getCharPositionInLine());
        parent.addChild(valueNode);
        ParseTreeNode realNNode = tree.newNode(valueNode, "realN", Double.toString(value), valNode.getSidx(), valNode.getEidx(), valNode.getLine(), valueNode.getCharPositionInLine());
        valueNode.addChild(realNNode);

        parent.delChild(valNode);
    }

    private static ParseTreeNode findValNode(ParseTreeNode pt, String variable) {
        Queue<ParseTreeNode> queue = new LinkedList<>();
        queue.add(pt);
        while (!queue.isEmpty()) {
            ParseTreeNode node = queue.poll();
            List<ParseTreeNode> children = node.getChildren();
            for (ParseTreeNode child : children) {
                if (child.getLabel().equals(variable)) {
                    return child;
                }
                queue.add(child);
            }
        }
        return null;
    }

}
