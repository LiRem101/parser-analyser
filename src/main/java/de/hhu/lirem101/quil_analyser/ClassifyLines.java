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

package de.hhu.lirem101.quil_analyser;

import org.snt.inmemantlr.tree.ParseTreeNode;

import java.util.*;
import java.util.stream.Collectors;

import static de.hhu.lirem101.quil_analyser.RulesOfParseTree.*;

/**
 * Class that classifies lines of a Quil file depending on the states they act on.
 * The classes are: Quantum, classical, quantum that influences classical and classical that influences quantum.
 */
public class ClassifyLines {

    private final Map<Integer, LineType> lineTypes = new java.util.HashMap<>();
    private final ParseTreeNode node;

    private boolean classificationCalculated = false;

    public ClassifyLines(ParseTreeNode node) {
        this.node = node;
        LinkedList<ParseTreeNode> nodeQueue = new LinkedList<>();
        Integer[] validCodelines = findValidCodelines(nodeQueue);
        for (int line : validCodelines) {
            lineTypes.put(line, null);
        }
    }

    private Integer[] findValidCodelines(Queue<ParseTreeNode> nodeQueue) {
        SortedSet<Integer> validCodelines = new TreeSet<>();
        ParseTreeNode currentNode = nodeQueue.poll();
        while (currentNode != null) {
            int line = currentNode.getLine();
            validCodelines.add(line);
            nodeQueue.addAll(currentNode.getChildren());
            currentNode = nodeQueue.poll();
        }
        return validCodelines.toArray(new Integer[0]);
    }

    public Map<Integer, LineType> classifyLines() {
        if(classificationCalculated) {
            return lineTypes;
        }
        calculateLineClassification();
        classificationCalculated = true;
        return lineTypes;
    }

    private void calculateLineClassification() {
        LinkedList<ParseTreeNode> nodeQueue = new LinkedList<>();
        nodeQueue.add(node);
        ParseTreeNode currentNode = nodeQueue.poll();
        while (currentNode != null) {
            int line = currentNode.getLine();
            String rule = currentNode.getRule();

            if(quantum.contains(rule)) {
                lineTypes.put(line, LineType.QUANTUM);
            } else if(classical.contains(rule)) {
                lineTypes.put(line, LineType.CLASSICAL);
            } else if(quantumInfluClassical.contains(rule)) {
                handleMeasurementNodes(currentNode, line);
            } else if(controlStructure.contains(rule)) {
                lineTypes.put(line, LineType.CONTROL_STRUCTURE);
            } else if(controlStructureClassical.contains(rule)) {
                lineTypes.put(line, LineType.CONTROL_STRUCTURE_INFLUENCED_CLASSICAL);
            } else if(rule.equals("gate") || rule.equals("circuitGate")) {
                handleGateNodes(currentNode, line);
            } else if(rule.equals("defCircuit")) {
                ParseTreeNode circuitChild = currentNode.getChildren().stream().filter(n -> n.getRule().equals("circuit")).findFirst().orElse(null);
                if(circuitChild != null) {
                    lineTypes.put(line, LineType.CONTROL_STRUCTURE);
                    nodeQueue.add(circuitChild);
                }
            } else {
                nodeQueue.addAll(currentNode.getChildren());
            }

            currentNode = nodeQueue.poll();
        }
    }

    /**
     * Handles the classification of measurement nodes.
     * Measurement only influences classical if addr is given in which the result is saved
     * @param currentNode The current node to classify.
     * @param line The line number of the current node.
     */
    private void handleMeasurementNodes(ParseTreeNode currentNode, int line) {
        ParseTreeNode addr = currentNode.getChildren().stream().filter(n -> n.getRule().equals("addr")).findFirst().orElse(null);
        if(addr != null) {
            lineTypes.put(line, LineType.QUANTUM_INFLUENCES_CLASSICAL);
        } else {
            lineTypes.put(line, LineType.QUANTUM);
        }
    }

    /**
     * Handles the classification of gate nodes. They are classified as quantum if they do not have a param node or if
     * the param node is just a number. Otherwise, they are classified as classical influences quantum.
     * @param currentNode The current node to classify.
     * @param line The line number of the current node.
     */
    private void handleGateNodes(ParseTreeNode currentNode, int line) {
        ParseTreeNode param = currentNode.getChildren().stream().filter(n -> n.getRule().equals("param")).findFirst().orElse(null);
        if(param == null) {
            lineTypes.put(line, LineType.QUANTUM);
        } else {
            ParseTreeNode addr = getAllChildNodes(param).stream().filter(n -> n.getRule().equals("addr")).findFirst().orElse(null);
            if(addr != null) {
                lineTypes.put(line, LineType.CLASSICAL_INFLUENCES_QUANTUM);
            } else {
                lineTypes.put(line, LineType.QUANTUM);
            }
        }
    }

    private ArrayList<ParseTreeNode> getAllChildNodes(ParseTreeNode node) {
        ArrayList<ParseTreeNode> children = new ArrayList<>();
        Queue<ParseTreeNode> nodeQueue = new LinkedList<>();
        nodeQueue.add(node);
        while (!nodeQueue.isEmpty()) {
            ParseTreeNode currentNode = nodeQueue.poll();
            children.add(currentNode);
            nodeQueue.addAll(currentNode.getChildren());
        }
        return children;
    }
}
