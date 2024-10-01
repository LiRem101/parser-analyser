package de.hhu.lirem101.quil_analyser;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.TreeBidiMap;

import java.util.*;

public class ControlFlowCreator {
    // The name of the labels and the line number in which they are defined.
    private final BidiMap<Integer, String> labels = new TreeBidiMap<>();
    // The name of the label a branch jumps to and the line number in which the jump is defined.
    private final Map<Integer, String> jumpsSameLevel = new HashMap<>();
    // The name of the label a conditional branch jumps to and the line number in which the jump is defined.
    private final Map<Integer, String> jumpsCondSameLevel = new HashMap<>();
    // The name of the circuit that is being called and the line number in which the circuit is called.
    private final Map<Integer, String> jumpToCircuits = new HashMap<>();
    // Code lines that are targeted/valid on this level.
    private final Set<Integer> validCodelines = new HashSet<>();
    // The name of a defined circuit and the line number in which the circuit is defined.
    private final BidiMap<Integer, String> linesCircuitsNextLevel = new TreeBidiMap<>();
    // The name of a defined circuit and the ControlFlowCreator that represents the circuit.
    private final Map<String, ControlFlowCreator> circuitsNextLevel = new HashMap<>();

    public ControlFlowCreator(OneLevelCodeBlock codeBlock) {
        labels.putAll(codeBlock.getLabels());
        jumpsSameLevel.putAll(codeBlock.getJumpsSameLevel());
        jumpsCondSameLevel.putAll(codeBlock.getJumpsCondSameLevel());
        jumpToCircuits.putAll(codeBlock.getJumpToCircuits());
        validCodelines.addAll(codeBlock.getValidCodelines());
        linesCircuitsNextLevel.putAll(codeBlock.getLinesCircuitsNextLevel());
        codeBlock.getCircuitsNextLevel().forEach((key, value) -> {
            ControlFlowCreator cfc = new ControlFlowCreator(value);
            cfc.addLabels(labels);
            circuitsNextLevel.put(key, cfc);
        });
    }

    public ControlFlowBlock createControlFlowBlock() {
        HashMap<String, ControlFlowBlock> hashmap = new HashMap<>();
        LinkedList<ControlFlowBlock> blockQueue = new LinkedList<>();
        ControlFlowBlock start = new ControlFlowBlock("start");
        ControlFlowBlock halt = new ControlFlowBlock("halt");
        hashmap.put("start", start);
        hashmap.put("halt", halt);
        int startline = validCodelines.stream().min(Integer::compareTo).orElse(0);
        createControlFlowBlock("start", startline, "halt", hashmap, blockQueue);
        return hashmap.get("start");
    }

    private void createControlFlowBlock(String name, int startline, String endBlockName, HashMap<String, ControlFlowBlock> blocks, LinkedList<ControlFlowBlock> blockQueue) {
        ControlFlowBlock block = blocks.get(name);
        int line = startline;
        boolean pollNextBlock = false;

        while(validCodelines.contains(line) || !blockQueue.isEmpty() || pollNextBlock) {
            pollNextBlock = false;
            if(!validCodelines.contains(line)) {
                block.addBranch(blocks.get(endBlockName));
                block = blockQueue.poll();
                line = block.getCodelines().get(0) + 1;
            }

            if(labels.containsKey(line)) {
                String label = labels.get(line);
                addBlockWithLabelNameIfNecessary(blocks, blockQueue, label, line);
                block.addBranch(blocks.get(label));
                pollNextBlock = true;
            } else {
                block.addCodeline(line);
            }

            if(jumpsSameLevel.containsKey(line) && !pollNextBlock) {
                String label = jumpsSameLevel.get(line);
                int labelLine = labels.getKey(label);
                addBlockWithLabelNameIfNecessary(blocks, blockQueue, label, labelLine);
                block.addBranch(blocks.get(label));
                pollNextBlock = true;
            } else if(jumpsCondSameLevel.containsKey(line) && !pollNextBlock) {
                String label = jumpsCondSameLevel.get(line);
                int labelLine = labels.getKey(label);
                addBlockWithLabelNameIfNecessary(blocks, blockQueue, label, labelLine);
                block.addBranch(blocks.get(label));

                String elseName = getNextBlockName(line, endBlockName, blocks, blockQueue);
                block.addBranch(blocks.get(elseName));
                pollNextBlock = true;
            } else if(jumpToCircuits.containsKey(line) && !pollNextBlock) {
                String nextBlockName = getNextBlockName(line, endBlockName, blocks, blockQueue);
                pollNextBlock = true;

                String label = jumpToCircuits.get(line);
                int circuitLine = linesCircuitsNextLevel.getKey(label);
                addBlockWithLabelNameIfNecessary(blocks, new LinkedList<>(), label, circuitLine);
                ControlFlowCreator circuitCreator = circuitsNextLevel.get(label);
                circuitCreator.createControlFlowBlock(label, circuitLine + 1, nextBlockName, blocks, new LinkedList<>());
                block.addBranch(blocks.get(label));
            }

            line++;

            if(pollNextBlock) {
                if(blockQueue.isEmpty()) {
                    line--;
                    break;
                }
                block = blockQueue.poll();
                line = block.getCodelines().get(0) + 1;
            }
        }

        if(!validCodelines.contains(line)) {
            block.addBranch(blocks.get(endBlockName));
        }

    }

    /**
     * Returns the name of the next block, which is either the next line or the next label name, if the next codeline is still valid.
     * @param line The current line.
     * @param endBlockName The name of the end block.
     * @param blocks The hashmap of blocks.
     * @param blockQueue The queue of blocks.
     * @return The name of the next block.
     */
    private String getNextBlockName(int line, String endBlockName, HashMap<String, ControlFlowBlock> blocks, LinkedList<ControlFlowBlock> blockQueue) {
        String nextBlockName = "line" + (line + 1);
        if (!validCodelines.contains(line + 1)) {
            nextBlockName = endBlockName;
        } else if (labels.containsKey(line + 1)) {
            nextBlockName = labels.get(line + 1);
            addBlockWithLabelNameIfNecessary(blocks, blockQueue, nextBlockName, line + 1);
        } else {
            addBlockWithLabelNameIfNecessary(blocks, blockQueue, nextBlockName, line + 1);
        }
        return nextBlockName;
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

    /**
     * Adds labels, used in Constructor for Labels of outer ControlFlowCreator.
     * @param labels A Map of labels.
     */
    private void addLabels(Map<Integer, String> labels) {
        this.labels.putAll(labels);
    }

}
