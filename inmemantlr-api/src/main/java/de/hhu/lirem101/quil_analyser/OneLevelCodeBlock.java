package de.hhu.lirem101.quil_analyser;

import org.snt.inmemantlr.tree.ParseTreeNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class OneLevelCodeBlock {

    private final HashMap<String, Integer> labels = new HashMap<>();
    private final HashMap<String, Integer> branchesSameLevel = new HashMap<>();
    private final HashSet<Integer> validCodelines = new HashSet<>();
    private final HashSet<String> definedGates = new HashSet<>();
    private final HashMap<String, Integer> linesCircuitsNextLevel = new HashMap<>();

    private final HashMap<String, OneLevelCodeBlock> circuitsNextLevel = new HashMap<>();

    public OneLevelCodeBlock(ParseTreeNode node) {
        List<ParseTreeNode> nodeQueue = new ArrayList<>();
        nodeQueue.add(node);
        while (!nodeQueue.isEmpty()) {
            ParseTreeNode currentNode = nodeQueue.remove(0);
            int line = currentNode.getLine();
            if (currentNode.getRule().equals("defCircuit")) {
                OneLevelCodeBlock circuit = new OneLevelCodeBlock(currentNode.getLastChild());
                // The Key is the name of the definied circuit
                String circuitName = currentNode.getFirstChild().getLabel();
                circuitsNextLevel.put(circuitName, circuit);
                linesCircuitsNextLevel.put(circuitName, line);
            } else if(currentNode.getRule().equals("defGate")) {
                definedGates.add(currentNode.getFirstChild().getLabel());
            } else {
                List<ParseTreeNode> newChildren = currentNode.getChildren();
                for (ParseTreeNode child : newChildren) {
                    nodeQueue.add(child);
                }
                if (newChildren.isEmpty()) {
                    validCodelines.add(line);
                }
            }

            if(currentNode.getRule().equals("defLabel")) {
                labels.put(currentNode.getFirstChild().getLabel(), line);
            } else if(currentNode.getRule().equals("jump") || currentNode.getRule().equals("jumpWhen") ||
                    currentNode.getRule().equals("jumpUnless")) {
                branchesSameLevel.put(currentNode.getFirstChild().getLabel(), line);
            }
        }
    }

}
