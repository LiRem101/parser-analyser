package de.hhu.lirem101.quil_optimizer;

import org.snt.inmemantlr.tree.ParseTreeNode;

import java.util.ArrayList;
import java.util.Map;

public class SortNodesIntoInstructions {
    private final Map<Integer, ParseTreeNode> sortedNodes;
    //private final ArrayList<ArrayList<InstructionNode>> instructions;

    public SortNodesIntoInstructions(Map<Integer, ParseTreeNode> nodes) {
        this.sortedNodes = nodes;
    }

    /**
     * Appends the node of the right line to the given instructions. A node may be appended to multiple instructions.
     * @param instructions The list of lists of instructions to append the node to.
     */
    public void appendNodeToInstructions(ArrayList<ArrayList<InstructionNode>> instructions) {
        for(ArrayList<InstructionNode> list : instructions) {
            for(InstructionNode node : list) {
                node.setParseTreeNode(sortedNodes.get(node.getLine()));
            }
        }
    }
}
