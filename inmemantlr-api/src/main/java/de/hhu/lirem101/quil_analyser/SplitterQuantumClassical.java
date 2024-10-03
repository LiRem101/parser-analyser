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

    private void calculateBlocks(HashMap<String, ControlFlowBlock> newBlocks, HashMap<String, ControlFlowBlock> originalBlocks, LinkedList<ControlFlowBlock> blockQueue) {
        while (!blockQueue.isEmpty()) {
            ControlFlowBlock currentBlock = blockQueue.poll();
            String currentBlockName = currentBlock.getName();
            ControlFlowBlock originalBlock = originalBlocks.get(currentBlockName);

            List<Integer> codelines = originalBlock.getCodelines();
            ControlFlowBlock quantumBlock = new ControlFlowBlock(currentBlockName + "quantum");
            quantumBlock.setLineType(LineType.QUANTUM);
            ControlFlowBlock classicalBlock = new ControlFlowBlock(currentBlockName + "classical");
            classicalBlock.setLineType(LineType.CLASSICAL);

            for (Integer codeline : codelines) {
                LineType type = classes.get(codeline);
                if (type == LineType.QUANTUM) {
                    quantumBlock.addCodeline(codeline);
                } else if (type == LineType.CLASSICAL) {
                    classicalBlock.addCodeline(codeline);
                } else if (type == LineType.CONTROL_STRUCTURE) {
                    if (!quantumBlock.getCodelines().isEmpty()) {
                        currentBlock.addBranch(quantumBlock);
                    }
                    if (!classicalBlock.getCodelines().isEmpty()) {
                        currentBlock.addBranch(classicalBlock);
                    }
                    if (currentBlock.getBranches().isEmpty()) {
                        currentBlock.addCodeline(codeline);
                    } else {
                        ControlFlowBlock newBlock = new ControlFlowBlock(currentBlockName + "control");
                        newBlock.setLineType(LineType.CONTROL_STRUCTURE);
                        newBlock.addCodeline(codeline);
                        if(currentBlock.getBranches().size() == 1) {
                            currentBlock.addBranch(newBlock);
                        } else {
                            quantumBlock.addBranch(newBlock);
                            classicalBlock.addBranch(newBlock);
                        }
                        currentBlock = newBlock;
                        quantumBlock = new ControlFlowBlock(currentBlockName + "quantum");
                        classicalBlock = new ControlFlowBlock(currentBlockName + "classical");
                    }
                }
            }

            ArrayList<ControlFlowBlock> lastBlocks = new ArrayList<>();
            if(!quantumBlock.getCodelines().isEmpty() || !classicalBlock.getCodelines().isEmpty()) {
                if(!quantumBlock.getCodelines().isEmpty()) {
                    currentBlock.addBranch(quantumBlock);
                    lastBlocks.add(quantumBlock);
                }
                if(!classicalBlock.getCodelines().isEmpty()) {
                    currentBlock.addBranch(classicalBlock);
                    lastBlocks.add(classicalBlock);
                }
            }
            if(lastBlocks.size() != 2) {
                lastBlocks.add(currentBlock);
            }

            for (ControlFlowBlock b : originalBlock.getBranches()) {
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
        }
    }

}
