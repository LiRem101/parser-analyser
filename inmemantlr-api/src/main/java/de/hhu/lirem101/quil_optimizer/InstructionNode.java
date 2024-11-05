package de.hhu.lirem101.quil_optimizer;

import de.hhu.lirem101.quil_analyser.DirectedGraphNode;
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

    public int getLine() {
        return line;
    }

    public ParseTreeNode getParseTreeNode() {
        return ptNode;
    }

    public ArrayList<String> getParameters() {
        ArrayList<String> parameters = new ArrayList<>();
        parameters.addAll(quantumParameters.keySet());
        parameters.addAll(classicalParameters.keySet());
        return parameters;
    }

    /**
     * Returns all next InstructionNodes from both the quantum and classical parameters.
     * @return ArrayList of next InstructionNodes
     */
    public ArrayList<InstructionNode> getNextInstructions() {
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

    /**
     * Adds the instruction's node to the instruction and extracts all parameters associated with the node.
     * @param ptNode The node to add to the instruction.
     */
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
                break;
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
                    this.classicalParameters.put(param, new connectedInstructions());
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

    /**
     * Sets the previous instruction node and the next instruction node for the given parameters. On the previous
     * instruction node, the next instruction node is set to this instruction node.
     * Changes the previous parameters such that it can be given to the next node.
     * @param previousParameters The previous instruction nodes.
     */
    public void setParameterLinks(Map<String, InstructionNode> previousParameters) {
        ArrayList<String> parameters = getParameters();
        for (String parameter : parameters) {
            if (!previousParameters.containsKey(parameter)) {
                // If parameter is not in the previousParameters, add it to previous Parameters
                previousParameters.put(parameter, this);
            } else {
                // If parameter is in the previousParameters, set the next instruction node for the parameter
                setPreviousInstruction(parameter, previousParameters.get(parameter));
                setNextInstruction(parameter, previousParameters.get(parameter));
                previousParameters.put(parameter, this);
            }
        }
    }

    /**
     * Sets the previous instruction node for the given parameter.
     * @param parameter The parameter to set the previous instruction node for.
     * @param previous The previous instruction node.
     */
    private void setPreviousInstruction(String parameter, InstructionNode previous) {
        if (quantumParameters.containsKey(parameter)) {
            quantumParameters.get(parameter).previous.add(previous);
        } else if (classicalParameters.containsKey(parameter)) {
            classicalParameters.get(parameter).previous.add(previous);
        } else {
            throw new IllegalArgumentException("Parameter " + parameter + " not found in instruction " + line);
        }
    }

    /**
     * Sets the next instruction node for the given parameter and previous instruction.
     * @param parameter The parameter to set the next instruction node for.
     * @param previous The previous instruction node.
     */
    private void setNextInstruction(String parameter, InstructionNode previous) {
        if (previous.quantumParameters.containsKey(parameter)) {
            previous.quantumParameters.get(parameter).next.add(this);
        } else if (previous.classicalParameters.containsKey(parameter)) {
            previous.classicalParameters.get(parameter).next.add(this);
        } else {
            throw new IllegalArgumentException("Parameter " + parameter + " not found in instruction " + line);
        }
    }
}
