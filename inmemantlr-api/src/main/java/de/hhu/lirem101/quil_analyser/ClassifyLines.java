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
    private static final Set<String> controlStructure = new HashSet<>(Arrays.asList("defLabel", "halt", "jump", "jumpWhen", "jumpUnless"));

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
                lineTypes.put(line, LineType.QUANTUM_INFLUENCES_CLASSICAL);
            } else if(controlStructure.contains(rule)) {
                lineTypes.put(line, LineType.CONTROL_STRUCTURE);
            } else {
                nodeQueue.addAll(currentNode.getChildren());
            }

            currentNode = nodeQueue.poll();
        }
    }
}
