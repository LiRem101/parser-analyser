package de.hhu.lirem101.quil_analyser;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.TreeBidiMap;

import java.util.*;

public class ControlFlowCreator {
    // The name of the labels and the line number in which they are defined.
    private final BidiMap<String, Integer> labels = new TreeBidiMap<>();
    // The name of the label a branch jumps to and the line number in which the jump is defined.
    private final BidiMap<String, Integer> jumpsSameLevel = new TreeBidiMap<>();
    // The name of the label a conditional branch jumps to and the line number in which the jump is defined.
    private final BidiMap<String, Integer> jumpsCondSameLevel = new TreeBidiMap<>();
    // The name of the circuit that is being called and the line number in which the circuit is called.
    private final Map<String, Integer> jumpToCircuits = new HashMap<>();
    // Code lines that are targeted/valid on this level.
    private final Set<Integer> validCodelines = new HashSet<>();
    // The name of a defined circuit and the line number in which the circuit is defined.
    private final Map<String, Integer> linesCircuitsNextLevel = new HashMap<>();
    // The name of a defined circuit and the OneLevelCodeBlock that represents the circuit.
    private final Map<String, OneLevelCodeBlock> circuitsNextLevel = new HashMap<>();

    public ControlFlowCreator(OneLevelCodeBlock codeBlock) {
        labels.putAll(codeBlock.getLabels());
        jumpsSameLevel.putAll(codeBlock.getJumpsSameLevel());
        jumpsCondSameLevel.putAll(codeBlock.getJumpsCondSameLevel());
        jumpToCircuits.putAll(codeBlock.getJumpToCircuits());
        validCodelines.addAll(codeBlock.getValidCodelines());
        linesCircuitsNextLevel.putAll(codeBlock.getLinesCircuitsNextLevel());
        circuitsNextLevel.putAll(codeBlock.getCircuitsNextLevel());
    }

    public ControlFlowBlock createControlFlowBlock() {
        HashMap<String, ControlFlowBlock> hashmap = new HashMap<>();
        LinkedList<ControlFlowBlock> blockQueue = new LinkedList<>();
        ControlFlowBlock start = new ControlFlowBlock("start");
        ControlFlowBlock halt = new ControlFlowBlock("halt");
        hashmap.put("start", start);
        hashmap.put("halt", halt);
        createControlFlowBlock("start", 0, "halt", hashmap, blockQueue);
        return hashmap.get("start");
    }

    private ControlFlowBlock createControlFlowBlock(String name, int startline, String endBlockName, HashMap<String, ControlFlowBlock> blocks, LinkedList<ControlFlowBlock> blockQueue) {
        ControlFlowBlock block = blocks.get(name);
        int line = startline;

        while(validCodelines.contains(line) || !blockQueue.isEmpty()) {
            boolean pollNextBlock = false;

            if(!validCodelines.contains(line)) {
                block.addBranch(blocks.get(endBlockName));
                block = blockQueue.poll();
                line = block.getCodelines().get(0) + 1;
            }

            if(labels.containsValue(line)) {
                String label = labels.getKey(line);
                addBlockWithLabelNameIfNecessary(blocks, blockQueue, label, labels.get(label));
                block.addBranch(blocks.get(label));
                pollNextBlock = true;
            } else {
                block.addCodeline(line);
            }

            if(jumpsSameLevel.containsValue(line)) {
                String label = jumpsSameLevel.getKey(line);
                addBlockWithLabelNameIfNecessary(blocks, blockQueue, label, labels.get(label));
                block.addBranch(blocks.get(label));
                pollNextBlock = true;
            } else if(jumpsCondSameLevel.containsValue(line)) {
                String label = jumpsCondSameLevel.getKey(line);
                addBlockWithLabelNameIfNecessary(blocks, blockQueue, label, labels.get(label));
                block.addBranch(blocks.get(label));

                String elseName = "line" + (line + 1);
                if(labels.containsValue(line + 1)) {
                    elseName = labels.getKey(line + 1);
                    addBlockWithLabelNameIfNecessary(blocks, blockQueue, elseName, labels.get(elseName));
                } else {
                    addBlockWithLabelNameIfNecessary(blocks, blockQueue, elseName, line + 1);
                }
                block.addBranch(blocks.get(elseName));
                pollNextBlock = true;
            }

            if(pollNextBlock) {
                if(blockQueue.isEmpty()) {
                    break;
                }
                block = blockQueue.poll();
                line = block.getCodelines().get(0);
            }
            line++;
        }

        if(!validCodelines.contains(line)) {
            block.addBranch(blocks.get(endBlockName));
        }

        return block;
    }

    /**
     * Adds a block with the given label name with given startline to the hashmap and the block queue if it does not exist yet.
     * @param blocks The hashmap of blocks.
     * @param blockQueue The queue of blocks.
     * @param label The label name of the block.
     * @param startline The startline of the new block.
     */
    private void addBlockWithLabelNameIfNecessary(HashMap<String, ControlFlowBlock> blocks, LinkedList<ControlFlowBlock> blockQueue, String label, int startline) {
        if(!blocks.containsKey(label)) {
            ControlFlowBlock nextBlock = new ControlFlowBlock(label);
            blocks.put(label, nextBlock);
            nextBlock.addCodeline(startline);
            blockQueue.add(nextBlock);
        }
    }

}