package de.hhu.lirem101.quil_analyser;

import java.util.*;

/**
 * Represents a control flow block that contains code lines and branches.
 */
public class ControlFlowBlock implements DirectedGraphNode {
    // Start label/name of the block.
    private final String name;
    // Rank of the block.
    private int rank = 0;
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

    public int getRank() {
        return rank;
    }

    public void setLineType(LineType type) {
        this.type = type;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public void setNewDominatingBlock(ControlFlowBlock block) {
        dominatingBlocks.add(block);
        dominatingBlocks.addAll(block.dominatingBlocks);
    }

    public boolean areAllDominatingBlocksIncluded(ArrayList<ControlFlowBlock> blocks) {
        return blocks.containsAll(dominatingBlocks);
    }

    public ArrayList<Integer> getAllDominatingLines() {
        ArrayList<Integer> lines = new ArrayList<>();
        // Sort blocks inversely by rank
        ArrayList<ControlFlowBlock> sortedBlocks = dominatingBlocks
                .stream()
                .sorted((b1, b2) -> Integer.compare(b2.rank, b1.rank))
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

        for (ControlFlowBlock block : sortedBlocks) {
            ArrayList<Integer> blockLines = block.getCodelines();
            Collections.reverse(blockLines);
            lines.addAll(blockLines);
        }
        return lines;
    }

    public ControlFlowBlock copyControlFlowBlock() {
        ControlFlowBlock copy = new ControlFlowBlock(name);
        copy.rank = rank;
        copy.codelines.addAll(codelines);
        copy.type = type;
        copy.dominatingBlocks.addAll(dominatingBlocks);
        for(ControlFlowBlock branch : branches) {
            ControlFlowBlock newBranch = branch.copyControlFlowBlock();
            copy.addBranch(newBranch);
        }
        return copy;
    }
}
