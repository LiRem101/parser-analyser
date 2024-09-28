package de.hhu.lirem101.quil_analyser;

import java.util.ArrayList;

/**
 * Represents a control flow block that contains code lines and branches.
 */
public class ControlFlowBlock {
    // List of code lines in this control flow block, in the order that they are being executed.
    private final ArrayList<Integer> codelines = new ArrayList<>();
    // Pointers to the next control flow blocks.
    private final ArrayList<ControlFlowBlock> branches = new ArrayList<>();

    public void addCodeline(int codeline) {
        codelines.add(codeline);
    }

    public void addBranch(ControlFlowBlock block) {
        branches.add(block);
    }

    public ArrayList<Integer> getCodelines() {
        return new ArrayList<>(codelines);
    }

    public ArrayList<ControlFlowBlock> getBranches() {
        return new ArrayList<>(branches);
    }
}
