package de.hhu.lirem101.quil_optimizer;

import de.hhu.lirem101.quil_analyser.DirectedGraphNode;
import de.hhu.lirem101.quil_analyser.LineParameter;
import de.hhu.lirem101.quil_analyser.LineType;
import org.snt.inmemantlr.tree.ParseTreeNode;

import java.util.*;
import java.util.stream.Collectors;

public class InstructionNode implements DirectedGraphNode<InstructionNode> {

    private class connectedInstructions {
        private final ArrayList<InstructionNode> previous = new ArrayList<>();
        private final ArrayList<InstructionNode> next = new ArrayList<>();
    }

    private final int line;
    private final LineType type;
    private ParseTreeNode ptNode;
    private final Map<String, connectedInstructions> quantumParameters = new HashMap<>();
    private final Map<String, connectedInstructions> classicalParameters = new HashMap<>();

    public InstructionNode(int line, LineType type) {
        this.line = line;
        this.type = type;
    }

    /**
     * Returns all previous InstructionNodes from both the quantum and classical parameters.
     * @return ArrayList of previous InstructionNodes
     */
    @Override
    public ArrayList<InstructionNode> getBranches() {
        Set<InstructionNode> branches = quantumParameters
                .values()
                .stream()
                .flatMap(x -> x.previous.stream())
                .collect(Collectors.toSet());
        branches.addAll(classicalParameters
                .values()
                .stream()
                .flatMap(x -> x.previous.stream())
                .collect(Collectors.toSet()));
        return new ArrayList<>(branches);
    }

    @Override
    public ArrayList<Integer> getCodelines() {
        return new ArrayList<>(Collections.singletonList(line));
    }

    @Override
    public String getName() {
        return "Line " + line;
    }

    @Override
    public LineType getLineType() {
        return type;
    }

    /**
     * Returns all next InstructionNodes from both the quantum and classical parameters.
     * @return ArrayList of next InstructionNodes
     */
    public ArrayList<InstructionNode> getnextInstructions() {
        Set<InstructionNode> branches = quantumParameters
                .values()
                .stream()
                .flatMap(x -> x.next.stream())
                .collect(Collectors.toSet());
        branches.addAll(classicalParameters
                .values()
                .stream()
                .flatMap(x -> x.next.stream())
                .collect(Collectors.toSet()));
        return new ArrayList<>(branches);
    }

    public void setParseTreeNode(ParseTreeNode ptNode) {
        this.ptNode = ptNode;
        calculateLineParameters(ptNode);
    }

    private void calculateLineParameters(ParseTreeNode node) {
        LinkedList<ParseTreeNode> queue = new LinkedList<>();
        queue.add(node);
        while(!queue.isEmpty()) {
            ParseTreeNode currentNode = queue.pollLast();
            handleParametersOfThisNode(currentNode);
            queue.addAll(currentNode.getChildren());
        }
    }

    private void handleParametersOfThisNode(ParseTreeNode node) {
        int line = node.getLine();
        switch (node.getRule()) {
            case "addr":
                if(this.line == line) {
                    this.classicalParameters.put(node.getLabel(), new connectedInstructions());
                }
            case "memoryDescriptor":
                String param = node.getLabel();
                // Remove 'DECLARE' in the front
                param = param.substring(7);
                // Remove BIT, FLOAT, INTEGER, OCTET or REAL in the middle
                param = param.replaceAll("[BIT|FLOAT|INTEGER|OCTET|REAL]", "");
                // Replace "[1]" with "[0]"
                // TODO: Make this work for all numbers
                param = param.replaceAll("\\[1\\]", "[0]");
                String finalParam = param;
                if(this.line == line) {
                    this.classicalParameters.put(node.getLabel(), new connectedInstructions());
                }
                break;
            case "qubit":
            case "qubitVariable":
                if(this.line == line) {
                    this.quantumParameters.put(node.getLabel(), new connectedInstructions());
                }
                break;
        }
    }
}
