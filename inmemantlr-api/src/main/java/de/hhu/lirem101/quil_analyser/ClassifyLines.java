package de.hhu.lirem101.quil_analyser;

import org.snt.inmemantlr.tree.ParseTreeNode;

import javax.sound.sampled.Line;
import java.util.*;

/**
 * Class that classifies lines of a Quil file depending on the states they act on.
 * The classes are: Quantum, classical, quantum that influences classical and classical that influences quantum.
 */
public class ClassifyLines {
    // Gate: Depends on param, just like circuitGate
    // Measure only influences classical if addr is given
    // Param (expression) classical (or rather C -> Q) if it is not exclusively a number

    private static final Set<String> quantum = new HashSet<>(Arrays.asList("defGate", "qubitVariable", "circuitQubit", "resetState", "circuitResetState"));
    private static final Set<String> classical = new HashSet<>(Arrays.asList("classicalUnary", "classicalBinary", "classicalComparison", "load", "store",
            "memoryDescriptor"));
    private static final Set<String> quantumInfluClassical = new HashSet<>(Arrays.asList("measure", "circuitMeasure"));
    private static final Set<String> controlStructure = new HashSet<>(Arrays.asList("defLabel", "defCircuit", "halt", "jump", "jumpWhen", "jumpUnless"));

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
            } else if(rule.equals("gate")) {
                handleGateNodes(currentNode, line);
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
            ParseTreeNode expression = param.getChildren().stream().filter(n -> n.getRule().equals("expression")).findFirst().orElse(null);
            if(expression == null || param.getChildren().size() != 1) {
                lineTypes.put(line, LineType.CLASSICAL_INFLUENCES_QUANTUM);
            } else {
                ParseTreeNode number = expression.getChildren().stream().filter(n -> n.getRule().equals("number")).findFirst().orElse(null);
                if(number == null || expression.getChildren().size() != 1) {
                    lineTypes.put(line, LineType.CLASSICAL_INFLUENCES_QUANTUM);
                } else {
                    lineTypes.put(line, LineType.QUANTUM);
                }
            }
        }
    }
}
