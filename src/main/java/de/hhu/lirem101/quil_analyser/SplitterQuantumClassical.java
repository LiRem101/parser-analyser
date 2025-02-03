/**
 * Quil Parser & Analyser
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2025 Lian Remme <lian.remme@dlr.de>
 *
 * SPDX-FileCopyrightText: 2025 Lian Remme <lian.remme@dlr.de>
 *
 * SPDX-License-Identifier: MIT
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

import javax.sound.sampled.Line;
import java.util.*;

public class SplitterQuantumClassical {

    private final ControlFlowBlock originalRoot;
    private final Map<Integer, LineType> classes;
    private ControlFlowBlock newRoot;
    private final Map<String, LineType> typesOfBlocks = new HashMap<>();
    boolean calculated = false;

    public SplitterQuantumClassical(ControlFlowBlock root, Map<Integer, LineType> classes) {
        this.originalRoot = root;
        this.classes = classes;
    }

    public ControlFlowBlock getNewNode() {
        if (!calculated) {
            calculated = true;
            calculateBlocks();
        }
        return newRoot;
    }

    /**
     * Calculate the new blocks based on the original blocks. The new blocks are divided into quantum blocks, classical
     * blocks and control blocks. The quantum blocks contain only quantum code lines, the classical blocks contain only
     * classical code lines. The original blocks do not have this distinction. Blocks without any lines (except first
     * and last block) are discarded.
     */
    private void calculateBlocks() {
        String name = "start";
        newRoot = new ControlFlowBlock(name);
        newRoot.setLineType(LineType.CONTROL_STRUCTURE);

        HashMap<String, ControlFlowBlock> originalBlocks = createBlockMap(originalRoot);
        HashMap<String, ControlFlowBlock> newBlocks = new HashMap<>();
        LinkedList<ControlFlowBlock> blockQueue = new LinkedList<>();
        newBlocks.put(name, newRoot);
        blockQueue.add(newRoot);
        calculateBlocks(newBlocks, originalBlocks, blockQueue);
    }

    /**
     * Create a map of the blocks in the control flow graph. The keys of the map are the names of the blocks.
     * @param root The root of the control flow graph.
     * @return A map of the blocks in the control flow graph.
     */
    private HashMap<String, ControlFlowBlock> createBlockMap(ControlFlowBlock root) {
        HashMap<String, ControlFlowBlock> blocks = new HashMap<>();
        LinkedList<ControlFlowBlock> blockQueue = new LinkedList<>();
        blockQueue.add(root);
        while (!blockQueue.isEmpty()) {
            ControlFlowBlock currentBlock = blockQueue.poll();
            blocks.put(currentBlock.getName(), currentBlock);
            for (ControlFlowBlock b : currentBlock.getBranches()) {
                if (!blocks.containsKey(b.getName())) {
                    blockQueue.add(b);
                }
            }
        }
        return blocks;
    }

    /**
     * Calculate the new blocks based on the original blocks. The new blocks are divided into quantum blocks, classical
     * blocks and control blocks. The quantum blocks contain only quantum code lines, the classical blocks contain only
     * classical code lines. The original blocks do not have this distinction. Blocks without any lines (except first
     * and last block) are discarded.
     * @param newBlocks The new blocks that are created.
     * @param originalBlocks The original blocks.
     * @param blockQueue A queue of blocks that need to be processed.
     */
    private void calculateBlocks(HashMap<String, ControlFlowBlock> newBlocks, HashMap<String, ControlFlowBlock> originalBlocks,
                                 LinkedList<ControlFlowBlock> blockQueue) {
        while (!blockQueue.isEmpty()) {
            ControlFlowBlock currentBlock = blockQueue.poll();
            String currentBlockName = currentBlock.getName();
            ControlFlowBlock originalBlock = originalBlocks.get(currentBlockName);

            List<Integer> codelines = originalBlock.getCodelines();
            int counter = 0;
            ControlFlowBlock quantumBlock = new ControlFlowBlock(currentBlockName + "quantum" + counter++);
            quantumBlock.setLineType(LineType.QUANTUM);
            ControlFlowBlock classicalBlock = new ControlFlowBlock(currentBlockName + "classical" + counter++);
            classicalBlock.setLineType(LineType.CLASSICAL);
            for (Integer codeline : codelines) {
                LineType type = classes.get(codeline);
                if (type == LineType.QUANTUM) {
                    quantumBlock.addCodeline(codeline);
                } else if (type == LineType.CLASSICAL) {
                    classicalBlock.addCodeline(codeline);
                } else {
                    ControlFlowBlock newBlock = new ControlFlowBlock(currentBlockName + "control" + counter++);
                    if (!quantumBlock.getCodelines().isEmpty()) {
                        currentBlock.addBranch(quantumBlock);
                        quantumBlock.addBranch(newBlock);
                    }
                    if (!classicalBlock.getCodelines().isEmpty()) {
                        currentBlock.addBranch(classicalBlock);
                        classicalBlock.addBranch(newBlock);
                    }
                    if (currentBlock.getBranches().isEmpty()) {
                        currentBlock.addCodeline(codeline);
                    } else {
                        newBlock.setLineType(LineType.CONTROL_STRUCTURE);
                        newBlock.addCodeline(codeline);
                        currentBlock = newBlock;
                        quantumBlock = new ControlFlowBlock(currentBlockName + "quantum" + counter++);
                        classicalBlock = new ControlFlowBlock(currentBlockName + "classical" + counter++);
                    }
                }
            }

            ArrayList<ControlFlowBlock> lastBlocks = new ArrayList<>();
            determineLastBlocks(quantumBlock, classicalBlock, currentBlock, lastBlocks);

            for (ControlFlowBlock b : originalBlock.getBranches()) {
                addBranches(newBlocks, blockQueue, b, lastBlocks);
            }
        }
        removeEmptyBlocks(newRoot);
    }

    /**
     * Determine the last blocks of the current block. If the quantum block or the classical block is not empty, the
     * corresponding block is added to the last blocks. If both blocks are empty, the current block is added to the
     * last blocks.
     * @param quantumBlock The quantum block.
     * @param classicalBlock The classical block.
     * @param currentBlock The current block.
     * @param lastBlocks The determined last blocks of the current block.
     */
    private void determineLastBlocks(ControlFlowBlock quantumBlock, ControlFlowBlock classicalBlock, ControlFlowBlock currentBlock, ArrayList<ControlFlowBlock> lastBlocks) {
        if(!quantumBlock.getCodelines().isEmpty() || !classicalBlock.getCodelines().isEmpty()) {
            if(!quantumBlock.getCodelines().isEmpty()) {
                currentBlock.addBranch(quantumBlock);
                lastBlocks.add(quantumBlock);
            }
            if(!classicalBlock.getCodelines().isEmpty()) {
                currentBlock.addBranch(classicalBlock);
                lastBlocks.add(classicalBlock);
            }
        } else {
            lastBlocks.add(currentBlock);
        }
    }

    /**
     * Add branches to the new blocks. The branches are added to the last blocks of the current block.
     * @param newBlocks The new blocks.
     * @param blockQueue A queue of blocks that gets new blocks if new blocks are created.
     * @param b The branch that gets the branches.
     * @param lastBlocks The last blocks of the current block. The block b will be added to them.
     */
    private void addBranches(HashMap<String, ControlFlowBlock> newBlocks, LinkedList<ControlFlowBlock> blockQueue, ControlFlowBlock b, ArrayList<ControlFlowBlock> lastBlocks) {
        String branchName = b.getName();
        if (!newBlocks.containsKey(branchName)) {
            ControlFlowBlock newBlock = new ControlFlowBlock(branchName);
            newBlock.setLineType(LineType.CONTROL_STRUCTURE);
            newBlocks.put(branchName, newBlock);
            blockQueue.add(newBlock);
        }
        for(ControlFlowBlock prevBlock : lastBlocks) {
            prevBlock.addBranch(newBlocks.get(branchName));
        }
    }

    /**
     * Remove empty blocks from the control flow graph. An empty block is a block that does not contain any code lines
     * and has branches. This only works if no two empty blocks follow after each other.
     * @param root The root of the control flow graph.
     */
    private void removeEmptyBlocks(ControlFlowBlock root) {
        HashMap<String, ControlFlowBlock> blocks = new HashMap<>();
        LinkedList<ControlFlowBlock> blockQueue = new LinkedList<>();
        blocks.put(root.getName(), root);
        blockQueue.add(root);
        while (!blockQueue.isEmpty()) {
            ControlFlowBlock currentBlock = blockQueue.poll();
            if (currentBlock.getBranches().isEmpty()) {
                continue;
            }
            for (ControlFlowBlock b : currentBlock.getBranches()) {
                if (b.getCodelines().isEmpty() && !b.getBranches().isEmpty()) {
                    currentBlock.removeBranch(b);
                    for (ControlFlowBlock bb : b.getBranches()) {
                        currentBlock.addBranch(bb);
                        if (!blocks.containsValue(bb) && !blockQueue.contains(bb)) {
                            blockQueue.add(bb);
                            blocks.put(b.getName(), b);
                        }
                    }
                }
                else if (!blocks.containsValue(b) && !blockQueue.contains(b)) {
                    blockQueue.add(b);
                    blocks.put(b.getName(), b);
                }
            }
        }
    }

}
