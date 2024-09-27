package de.hhu.lirem101.quil_analyser;

import org.snt.inmemantlr.tree.ParseTreeNode;

import java.util.*;

/**
 * Represents a code block at one level of the parse tree.
 * This class processes a parse tree node and categorizes its children
 * into labels, branches, valid code lines, defined gates, and circuits.
 */
public class OneLevelCodeBlock {

    private final Map<String, Integer> labels = new HashMap<>();
    private final Map<String, Integer> branchesSameLevel = new HashMap<>();
    private final Set<Integer> validCodelines = new HashSet<>();
    private final Set<String> definedGates = new HashSet<>();
    private final Map<String, Integer> linesCircuitsNextLevel = new HashMap<>();
    private final Map<String, OneLevelCodeBlock> circuitsNextLevel = new HashMap<>();

    /**
     * Constructs a OneLevelCodeBlock by processing the given parse tree node.
     *
     * @param node the root parse tree node to process
     */
    public OneLevelCodeBlock(ParseTreeNode node) {
        Queue<ParseTreeNode> nodeQueue = new LinkedList<>();
        nodeQueue.add(node);
        while (!nodeQueue.isEmpty()) {
            ParseTreeNode currentNode = nodeQueue.poll();
            int line = currentNode.getLine();
            String rule = currentNode.getRule();

            switch (rule) {
                case "defCircuit":
                    OneLevelCodeBlock circuit = new OneLevelCodeBlock(currentNode.getLastChild());
                    circuitsNextLevel.put(currentNode.getFirstChild().getLabel(), circuit);
                    linesCircuitsNextLevel.put(currentNode.getFirstChild().getLabel(), line);
                    break;
                case "defGate":
                    definedGates.add(currentNode.getFirstChild().getLabel());
                    break;
                case "defLabel":
                    labels.put(currentNode.getFirstChild().getLabel(), line);
                    nodeQueue.addAll(currentNode.getChildren());
                    break;
                case "jump":
                case "jumpWhen":
                case "jumpUnless":
                    branchesSameLevel.put(currentNode.getFirstChild().getLabel(), line);
                    nodeQueue.addAll(currentNode.getChildren());
                    break;
                default:
                    nodeQueue.addAll(currentNode.getChildren());
                    if (currentNode.getChildren().isEmpty()) {
                        validCodelines.add(line);
                    }
                    break;
            }
        }
    }
}
