package de.hhu.lirem101.quil_analyser;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.TreeBidiMap;
import org.snt.inmemantlr.tree.ParseTreeNode;
import java.util.*;

/**
 * Represents a code block at one level of the parse tree.
 * This class processes a parse tree node and categorizes its children
 * into labels, branches, valid code lines, defined gates, and circuits.
 */
public class OneLevelCodeBlock {
    // The name of the labels and the line number in which they are defined.
    private final Map<Integer, String> labels = new HashMap<>();
    // The name of the label a branch jumps to and the line number in which the jump is defined.
    private final Map<Integer, String> jumpsSameLevel = new HashMap<>();
    // The name of the label a conditional branch jumps to and the line number in which the jump is defined.
    private final Map<Integer, String> jumpsCondSameLevel = new HashMap<>();
    // The name of the circuit that is being called and the line number in which the circuit is called.
    private final Map<Integer, String> jumpToCircuits = new HashMap<>();
    // Code lines that are targeted/valid on this level.
    private final Set<Integer> validCodelines = new HashSet<>();
    // The names of the gates that are manually defined on this level (via defGate).
    private final Set<String> definedGates = new HashSet<>();
    // The name of a defined circuit and the line number in which the circuit is defined.
    private final Map<Integer, String> linesCircuitsNextLevel = new HashMap<>();
    // The name of a defined circuit and the ControlFlowCreator that represents the circuit.
    private final Map<String, OneLevelCodeBlock> circuitsNextLevel = new HashMap<>();

    /**
     * Constructs a OneLevelCodeBlock by processing the given parse tree node.
     *
     * @param node the root parse tree node to process
     */
    public OneLevelCodeBlock(ParseTreeNode node) {
        Queue<ParseTreeNode> nodeQueue = new LinkedList<>();
        nodeQueue.add(node);
        parseAst(nodeQueue);
        nodeQueue.add(node);
        findCircuitJumps(nodeQueue);
    }

    /**
     * Extracts properties from the parse tree node. Updates the variables labels, branchesSameLevel, validCodelines,
     * definedGates, linesCircuitsNextLevel, and circuitsNextLevel.
     */
    private void parseAst(Queue<ParseTreeNode> nodeQueue) {
        ParseTreeNode currentNode = nodeQueue.poll();
        while (currentNode != null) {
            int line = currentNode.getLine();
            String rule = currentNode.getRule();

            switch (rule) {
                case "defCircuit":
                    OneLevelCodeBlock circuit = new OneLevelCodeBlock(currentNode.getLastChild());
                    circuitsNextLevel.put(currentNode.getFirstChild().getLabel(), circuit);
                    linesCircuitsNextLevel.put(line, currentNode.getFirstChild().getLabel());
                    break;
                case "defGate":
                    definedGates.add(currentNode.getFirstChild().getLabel());
                    break;
                case "defLabel":
                    labels.put(line, currentNode.getFirstChild().getLabel());
                    nodeQueue.addAll(currentNode.getChildren());
                    break;
                case "jump":
                    jumpsSameLevel.put(line, currentNode.getFirstChild().getLabel());
                    nodeQueue.addAll(currentNode.getChildren());
                    break;
                case "jumpWhen":
                case "jumpUnless":
                    jumpsCondSameLevel.put(line, currentNode.getFirstChild().getLabel());
                    nodeQueue.addAll(currentNode.getChildren());
                    break;
                default:
                    nodeQueue.addAll(currentNode.getChildren());
                    if (currentNode.getChildren().isEmpty()) {
                        validCodelines.add(line);
                    }
                    break;
            }
            currentNode = nodeQueue.poll();
        }
    }

    /**
     * Finds the lines from which to jump to circuits.
     */
    private void findCircuitJumps(Queue<ParseTreeNode> nodeQueue) {
        ParseTreeNode currentNode = nodeQueue.poll();
        while (currentNode != null) {
            String rule = currentNode.getRule();

            if(rule.equals("gate")) {
                String gateName = currentNode.getFirstChild().getLabel();
                if(circuitsNextLevel.containsKey(gateName)) {
                    jumpToCircuits.put(currentNode.getLine(), gateName);
                }
            }
            nodeQueue.addAll(currentNode.getChildren());

            currentNode = nodeQueue.poll();
        }
    }

    public Map<Integer, String> getLabels() {
        return new HashMap<>(labels);
    }

    public Map<Integer, String> getJumpsSameLevel() {
        return new HashMap<>(jumpsSameLevel);
    }

    public Map<Integer, String> getJumpsCondSameLevel() {
        return new HashMap<>(jumpsCondSameLevel);
    }

    public Map<Integer, String> getJumpToCircuits() {
        return new HashMap<>(jumpToCircuits);
    }

    public Set<Integer> getValidCodelines() {
        return new HashSet<>(validCodelines);
    }

    public Set<String> getDefinedGates() {
        return new HashSet<>(definedGates);
    }

    public Map<Integer, String> getLinesCircuitsNextLevel() {
        return new HashMap<>(linesCircuitsNextLevel);
    }

    public Map<String, OneLevelCodeBlock> getCircuitsNextLevel() {
        return new HashMap<>(circuitsNextLevel);
    }
}
