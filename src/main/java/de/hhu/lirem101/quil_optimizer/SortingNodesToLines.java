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
