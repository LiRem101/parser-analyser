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

    private final BidiMap<String, Integer> labels = new TreeBidiMap<>();
    private final BidiMap<String, Integer> branchesSameLevel = new TreeBidiMap<>();
    private final BidiMap<String, Integer> branchesCondSameLevel = new TreeBidiMap<>();
    private final Map<String, Integer> jumpToCircuits = new HashMap<>();
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
                    branchesSameLevel.put(currentNode.getFirstChild().getLabel(), line);
                    nodeQueue.addAll(currentNode.getChildren());
                    break;
                case "jumpWhen":
                case "jumpUnless":
                    branchesCondSameLevel.put(currentNode.getFirstChild().getLabel(), line);
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
                if(linesCircuitsNextLevel.containsKey(gateName)) {
                    jumpToCircuits.put(gateName, currentNode.getLine());
                }
            }
            nodeQueue.addAll(currentNode.getChildren());

            currentNode = nodeQueue.poll();
        }
    }

    public BidiMap<String, Integer> getLabels() {
        return new TreeBidiMap<>(labels);
    }

    public BidiMap<String, Integer> getBranchesSameLevel() {
        return new TreeBidiMap<>(branchesSameLevel);
    }

    public BidiMap<String, Integer> getBranchesCondSameLevel() {
        return new TreeBidiMap<>(branchesCondSameLevel);
    }

    public Map<String, Integer> getJumpToCircuits() {
        return new HashMap<>(jumpToCircuits);
    }

    public Set<Integer> getValidCodelines() {
        return new HashSet<>(validCodelines);
    }

    public Set<String> getDefinedGates() {
        return new HashSet<>(definedGates);
    }

    public Map<String, Integer> getLinesCircuitsNextLevel() {
        return new HashMap<>(linesCircuitsNextLevel);
    }

    public Map<String, OneLevelCodeBlock> getCircuitsNextLevel() {
        return new HashMap<>(circuitsNextLevel);
    }
}
