/**
 * Quil Parser & Analyser
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2025 Lian Remme <lian.remme@dlr.de>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 **/

package de.hhu.lirem101.quil_analyser;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.TreeBidiMap;

import java.util.*;

public class ControlFlowCreator {
    private final String suffix;
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
    // The name of a defined circuit and the ControlFlowBlock that represents the circuit.
    private final Map<String, OneLevelCodeBlock> circuitsNextLevel = new HashMap<>();

    public ControlFlowCreator(OneLevelCodeBlock codeBlock) {
        this.suffix = "";
        labels.putAll(codeBlock.getLabels());
        jumpsSameLevel.putAll(codeBlock.getJumpsSameLevel());
        jumpsCondSameLevel.putAll(codeBlock.getJumpsCondSameLevel());
        jumpToCircuits.putAll(codeBlock.getJumpToCircuits());
        validCodelines.addAll(codeBlock.getValidCodelines());
        linesCircuitsNextLevel.putAll(codeBlock.getLinesCircuitsNextLevel());
        circuitsNextLevel.putAll(codeBlock.getCircuitsNextLevel());
    }

    private ControlFlowCreator(String suffix, OneLevelCodeBlock codeBlock, BidiMap<Integer, String> higherUpLabels) {
        this.suffix = suffix;
        addWithSuffix(suffix, codeBlock.getLabels(), this.labels);
        labels.putAll(higherUpLabels);
        addWithSuffix(suffix, codeBlock.getJumpsSameLevel(), this.jumpsSameLevel);
        addWithSuffix(suffix, codeBlock.getJumpsCondSameLevel(), this.jumpsCondSameLevel);
        addWithSuffix(suffix, codeBlock.getJumpToCircuits(), this.jumpToCircuits);
        this.validCodelines.addAll(codeBlock.getValidCodelines());
        addWithSuffix(suffix, codeBlock.getLinesCircuitsNextLevel(), this.linesCircuitsNextLevel);
        codeBlock.getCircuitsNextLevel().forEach((key, value) -> {
            circuitsNextLevel.put(key + suffix, value);
        });
    }

    private void addWithSuffix(String suffix, Map<Integer, String> parameterMap, Map<Integer, String> objectMap) {
        parameterMap.forEach((key, value) -> {
            objectMap.put(key, value + suffix);
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
        int suffixCounter = 0;
        boolean pollNextBlock = false;
        boolean addLastBlock = true;

        while(validCodelines.contains(line) || !blockQueue.isEmpty() || pollNextBlock) {
            boolean polled = false;
            if(pollNextBlock || !validCodelines.contains(line)) {
                if(blockQueue.isEmpty()) {
                    addLastBlock = false;
                    break;
                }
                if(!validCodelines.contains(line)) {
                    block.addBranch(blocks.get(endBlockName));
                }
                block = blockQueue.poll();
                line = block.getCodelines().get(0);
                polled = true;
            }

            pollNextBlock = false;

            if(labels.containsKey(line) && !polled) {
                String label = labels.get(line);
                addBlockWithLabelNameIfNecessary(blocks, blockQueue, label, line);
                block.addBranch(blocks.get(label));
                pollNextBlock = true;
            } else if(!polled) {
                block.addCodeline(line);
            }

            if(jumpsSameLevel.containsKey(line) && !pollNextBlock) {
                String label = jumpsSameLevel.get(line);
                label = getLabelFromThisOrUpperLevel(label);
                int labelLine = labels.getKey(label);
                addBlockWithLabelNameIfNecessary(blocks, blockQueue, label, labelLine);
                block.addBranch(blocks.get(label));
                pollNextBlock = true;
            } else if(jumpsCondSameLevel.containsKey(line) && !pollNextBlock) {
                String label = jumpsCondSameLevel.get(line);
                label = getLabelFromThisOrUpperLevel(label);
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
                String circuitSuffix = "_" + suffixCounter;
                String circuitName = label + circuitSuffix;
                addBlockWithLabelNameIfNecessary(blocks, new LinkedList<>(), circuitName, circuitLine);
                OneLevelCodeBlock circuitBlock = circuitsNextLevel.get(label);
                ControlFlowCreator circuitCreator = new ControlFlowCreator(suffix + circuitSuffix, circuitBlock, labels);
                circuitCreator.createControlFlowBlock(circuitName, circuitLine + 1, nextBlockName, blocks, new LinkedList<>());
                block.addBranch(blocks.get(circuitName));
                suffixCounter++;
            }
            int finalLine = line;
            line = validCodelines.stream().filter(l -> (l > finalLine)).min(Integer::compareTo).orElse(-1);
        }

        if(addLastBlock) {
            block.addBranch(blocks.get(endBlockName));
        }

    }

    /**
     * Returns the label name without the suffix if the label is not defined on this level.
     * @param label The label name.
     * @return The label name without the suffix.
     */
    private String getLabelFromThisOrUpperLevel(String label) {
        if(!labels.containsValue(label)) {
            int suffixIndex = label.lastIndexOf(suffix);
            if(suffixIndex != -1) {
                label = label.substring(0, suffixIndex);
            }
        }
        return label;
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
