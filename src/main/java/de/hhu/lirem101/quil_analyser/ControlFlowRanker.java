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

import java.util.*;
import java.util.stream.Collectors;

public class ControlFlowRanker {

    private boolean calculated = false;
    private final ControlFlowBlock root;
    private final ArrayList<ControlFlowBlock> ranking = new ArrayList<>();
    private final Set<Integer> conditionalJumps;
    private ArrayList<ControlFlowBlock> startBlocks = new ArrayList<>();

    public ControlFlowRanker(ControlFlowBlock root, ArrayList<LineParameter> lines) {
        this.root = root;
        conditionalJumps = lines
                .stream()
                .filter(line -> line.getLineType() == LineType.CONTROL_STRUCTURE_INFLUENCED_CLASSICAL)
                .map(LineParameter::getLineNumber)
                .collect(Collectors.toSet());
    }

    /**
     * Create a list of all start blocks, after cutting branches from blocks with conditional jumps.
     */
    private ArrayList<ControlFlowBlock> getStartBlocks(ControlFlowBlock root) {
        ArrayList<ControlFlowBlock> startBlocks = new ArrayList<>();
        startBlocks.add(root);
        Queue<ControlFlowBlock> queue = new LinkedList<>();
        queue.add(root);
        while(!queue.isEmpty()) {
            ControlFlowBlock currentBlock = queue.poll();
            boolean hasConditionalJump = !currentBlock.getCodelines().isEmpty() && conditionalJumps.contains(currentBlock.getCodelines().get(currentBlock.getCodelines().size() - 1));
            if(hasConditionalJump) {
                ArrayList<ControlFlowBlock> originalBranches = new ArrayList<>(currentBlock.getBranches());
                for (ControlFlowBlock child : currentBlock.getBranches()) {
                    currentBlock.removeBranch(child);
                }
                for (ControlFlowBlock child : originalBranches) {
                    ControlFlowBlock newChild = child.copyControlFlowBlock();
                    queue.add(newChild);
                    startBlocks.add(newChild);
                }
            } else {
                for (ControlFlowBlock child : currentBlock.getBranches()) {
                    if (!queue.contains(child)) {
                        queue.add(child);
                    }
                }
            }
        }
        return startBlocks;
    }

    /**
     * This method determines the dominating block for each block in the control flow graph. The dominating blocks are
     * the blocks that are on the path from the start block to the current block.
     * @param startBlocks The list of start blocks.
     */
    private void determineDominatingBlocks(ArrayList<ControlFlowBlock> startBlocks) {
        for (ControlFlowBlock startBlock : startBlocks) {
            Queue<ControlFlowBlock> queue = new LinkedList<>();
            queue.add(startBlock);
            while(!queue.isEmpty()) {
                ControlFlowBlock currentBlock = queue.poll();
                for(ControlFlowBlock child : currentBlock.getBranches()) {
                    if(!queue.contains(child)) {
                        queue.add(child);
                    }
                    child.setNewDominatingBlock(currentBlock);
                }
            }
        }
    }

    /**
     * This method calculates the ranking of the control flow blocks. All blocks between a block and the starting block
     * have to have a lower ranking than the block itself. Blocks with a conditional jump at the end have their branches
     * removed before that, to remove circles. This could end into multiple unconnected graphs.
     */
    private void calculateRanking() {
        startBlocks = getStartBlocks(root);
        determineDominatingBlocks(startBlocks);
        Queue<ControlFlowBlock> queue = new LinkedList<>();
        for (ControlFlowBlock startBlock : startBlocks) {
            queue.add(startBlock);
            while(!queue.isEmpty()) {
                ControlFlowBlock currentBlock = queue.poll();
                if(ranking.contains(currentBlock)) {
                    continue;
                }
                if(currentBlock.areAllDominatingBlocksIncluded(ranking)) {
                    ranking.add(currentBlock);
                    for(ControlFlowBlock child : currentBlock.getBranches()) {
                        if(!queue.contains(child)) {
                            queue.add(child);
                        }
                    }
                } else {
                    queue.add(currentBlock);
                }
            }
        }

        for(int i = 0; i < ranking.size(); i++) {
            ranking.get(i).setRank(i);
        }
    }

    public ArrayList<ControlFlowBlock> getRankedBlocks() {
        if(!calculated) {
            calculated = true;
            calculateRanking();
        }
        return ranking;
    }

    public ArrayList<Integer> getIndizesOfStartBlocks() {
        if(!calculated) {
            calculated = true;
            calculateRanking();
        }
        return startBlocks.stream().map(ControlFlowBlock::getRank).collect(Collectors.toCollection(ArrayList::new));
    }
}
