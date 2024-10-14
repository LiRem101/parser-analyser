package de.hhu.lirem101.quil_analyser;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class ControlFlowRanker {

    private boolean calculated = false;
    private final ControlFlowBlock root;
    private final ArrayList<ControlFlowBlock> ranking = new ArrayList<>();

    public ControlFlowRanker(ControlFlowBlock root) {
        this.root = root;
    }


    // This is not yet circle-safe
    private void determineDominatingBlocks() {
        Queue<ControlFlowBlock> queue = new LinkedList<>();
        queue.add(root);
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

    private void calculateRanking() {
        determineDominatingBlocks();
        Queue<ControlFlowBlock> queue = new LinkedList<>();
        queue.add(root);
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
}
