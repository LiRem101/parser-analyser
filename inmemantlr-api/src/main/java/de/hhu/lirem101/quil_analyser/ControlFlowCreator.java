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

        while(validCodelines.contains(line)) {
            if(labels.containsValue(line)) {
                String label = labels.getKey(line);
                if(!blocks.containsKey(label)) {
                    ControlFlowBlock nextBlock = new ControlFlowBlock(label);
                    blocks.put(label, nextBlock);
                    nextBlock.addCodeline(labels.get(label));
                    blockQueue.add(nextBlock);
                }
                block.addBranch(blocks.get(label));
                if(blockQueue.isEmpty()) {
                    break;
                }
                block = blockQueue.poll();
                line = block.getCodelines().get(0);
            } else if(jumpsSameLevel.containsValue(line)) {
                String label = jumpsSameLevel.getKey(line);
                block.addCodeline(line);
                if(!blocks.containsKey(label)) {
                    ControlFlowBlock nextBlock = new ControlFlowBlock(label);
                    blocks.put(label, nextBlock);
                    nextBlock.addCodeline(labels.get(label));
                    blockQueue.add(nextBlock);
                }
                block.addBranch(blocks.get(label));
                if(blockQueue.isEmpty()) {
                    break;
                }
                block = blockQueue.poll();
                line = block.getCodelines().get(0);
            } else if(jumpsCondSameLevel.containsValue(line)) {
                String label = jumpsCondSameLevel.getKey(line);
                block.addCodeline(line);
                if(!blocks.containsKey(label)) {
                    ControlFlowBlock nextBlock = new ControlFlowBlock(label);
                    blocks.put(label, nextBlock);
                    nextBlock.addCodeline(labels.get(label));
                    blockQueue.add(nextBlock);
                }
                block.addBranch(blocks.get(label));

                int nextLine = line + 1;
                String elseName = "line" + nextLine;
                ControlFlowBlock elseBlock = new ControlFlowBlock(elseName);
                blocks.put(elseName, elseBlock);
                block.addBranch(elseBlock);
                block = elseBlock;

            } else {
                block.addCodeline(line);
            }
            line++;
        }

        block.addBranch(blocks.get(endBlockName));
        return block;
    }

}
