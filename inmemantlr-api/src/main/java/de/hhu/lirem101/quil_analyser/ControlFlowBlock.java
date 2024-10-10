package de.hhu.lirem101.quil_analyser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents a control flow block that contains code lines and branches.
 */
public class ControlFlowBlock {
    // Start label/name of the block.
    private final String name;
    // List of code lines in this control flow block, in the order that they are being executed.
    private final ArrayList<Integer> codelines = new ArrayList<>();
    // Pointers to the next control flow blocks.
    private final ArrayList<ControlFlowBlock> branches = new ArrayList<>();
    // What kind of block this is.
    private LineType type = null;
    // Which blocks are between this node and start
    private final Set<ControlFlowBlock> dominatingBlocks = new HashSet<>();

    public ControlFlowBlock(String name) {
        this.name = name;
    }

    public void addCodeline(int codeline) {
        codelines.add(codeline);
    }

    public void addCodelines(List<Integer> codelines) {
        this.codelines.addAll(codelines);
    }

    public void addBranch(ControlFlowBlock block) {
        branches.add(block);
    }

    public void removeBranch(ControlFlowBlock block) {
        branches.remove(block);
    }

    public ArrayList<Integer> getCodelines() {
        return new ArrayList<>(codelines);
    }

    public ArrayList<ControlFlowBlock> getBranches() {
        return new ArrayList<>(branches);
    }

    public String getName() {
        return name;
    }

    public LineType getLineType() {
        return type;
    }

    public void setLineType(LineType type) {
        this.type = type;
    }

    public void setNewDominatingBlock(ControlFlowBlock block) {
        dominatingBlocks.add(block);
        dominatingBlocks.addAll(block.dominatingBlocks);
    }

    public boolean areAllDominatingBlocksIncluded(ArrayList<ControlFlowBlock> blocks) {
        return blocks.containsAll(dominatingBlocks);
    }
}
