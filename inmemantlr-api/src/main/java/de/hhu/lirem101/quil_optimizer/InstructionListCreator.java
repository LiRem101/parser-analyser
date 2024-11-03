package de.hhu.lirem101.quil_optimizer;

import de.hhu.lirem101.quil_analyser.ControlFlowBlock;
import de.hhu.lirem101.quil_analyser.LineType;
import org.snt.inmemantlr.tree.ParseTreeNode;

import java.util.*;

public class InstructionListCreator {
    private final ControlFlowBlock block;
    private final Map<Integer, LineType> classes;
    // A list holding all instructions of the program divided into sections without conditional jumps
    // And sorted by the order of execution
    private ArrayList<ArrayList<InstructionNode>> instructions;
    boolean calculated = false;

    public InstructionListCreator(ControlFlowBlock block, Map<Integer, LineType> classes) {
        this.block = block;
        this.classes = classes;
    }

    /**
     * Creates list of lists of instructions from the control flow block and the AST. The list returned.
     * @return The instructions of the program.
     */
    public ArrayList<ArrayList<InstructionNode>> getInstructions() {
        if (!calculated) {
            ArrayList<ArrayList<Integer>> allLines = calculateExecutionOrder();
            instructions = calculateInstructions(allLines);
            calculated = true;
        }
        return copyInstructions(instructions);
    }


    /**
     * Copies a list of lists of instructions.
     * @param instructions The list of lists of instructions to be copied.
     * @return The copied list of lists of instructions.
     */
    public static ArrayList<ArrayList<InstructionNode>> copyInstructions(ArrayList<ArrayList<InstructionNode>> instructions) {
        ArrayList<ArrayList<InstructionNode>> copy = new ArrayList<>();
        for (ArrayList<InstructionNode> list : instructions) {
            ArrayList<InstructionNode> copyList = new ArrayList<>();
            copyList.addAll(list);
            copy.add(copyList);
        }
        return copy;
    }


    /**
     * Creates lists of instructions from the control flow block. The instructions are returned.
     * @return The instructions of the program.
     */
    private ArrayList<ArrayList<InstructionNode>> calculateInstructions(ArrayList<ArrayList<Integer>> allLines) {
        ArrayList<ArrayList<InstructionNode>> allInstructions = new ArrayList<>();
        for(ArrayList<Integer> lines : allLines) {
            ArrayList<InstructionNode> currentInstructions = new ArrayList<>();
            for(int line : lines) {
                LineType type = classes.get(line);
                InstructionNode node = new InstructionNode(line, type);
                currentInstructions.add(node);
            }
            allInstructions.add(currentInstructions);
        }
        return allInstructions;
    }

    /**
     * Calculates the lines of the program in the order of execution. The lines are divided into sections without
     * conditional jumps and sorted by the order of execution.
     * @return The lines of the program in the order of execution.
     */
    private ArrayList<ArrayList<Integer>> calculateExecutionOrder() {
        ControlFlowBlock currentBlock = block;
        ArrayList<Integer> currentLines = new ArrayList<>();
        ArrayList<ArrayList<Integer>> allLines = new ArrayList<>();
        allLines.add(currentLines);
        // Queues blocks before the next conditional jump
        Queue<ControlFlowBlock> currentBlocks = new LinkedList<>();
        ArrayList<Queue<ControlFlowBlock>> queues = new ArrayList<>();
        queues.add(currentBlocks);
        Set<Integer> handledConditionalJumps = new HashSet<>();
        int indexOfLastControlStructure = 0;
        while(!queues.isEmpty() && currentBlock != null) {
            // If currentBlock has codelines, add them to currentLines
            // Do not add non-conditional control structures to currentLines
            // If the line is a control structure (conditional and non-cond), sort all lines between this control
            // structure and the last one
            if(currentBlock.getCodelines().isEmpty()) {
                indexOfLastControlStructure = addCodelinesToInstructions(currentBlock, currentLines, indexOfLastControlStructure);
            }

            // If the last line of currentBlock is a conditional jump, add the two succeeding blocks to two next queues
            // If this conditional jump has not already been handled
            int lastLine = currentBlock.getCodelines().get(currentBlock.getCodelines().size() - 1);
            if(classes.get(lastLine) == LineType.CONTROL_STRUCTURE_INFLUENCED_CLASSICAL && !handledConditionalJumps.contains(lastLine)) {
                handledConditionalJumps.add(lastLine);
                putBranchesInQueue(currentBlock, queues);
            } else {
                // Add branches to current Queue
                currentBlocks.addAll(currentBlock.getBranches());
            }

            // If currentBlocks is empty, remove from queues and take the next block from the next queue
            // Set indexOfLastControlStructure to zero
            if(currentBlocks.isEmpty()) {
                queues.remove(0);
                if(!queues.isEmpty()) {
                    currentBlocks = queues.get(0);
                    currentBlock = currentBlocks.poll();
                    currentLines = new ArrayList<>();
                    allLines.add(currentLines);
                    indexOfLastControlStructure = 0;
                }
            } else {
                currentBlock = currentBlocks.poll();
            }
        }
        return allLines;
    }

    /**
     * Adds the two branches of a block to new queues and add these queues to the list of queues.
     * @param currentBlock The block with the branches to be added.
     * @param queues The queues to which the new queues are added.
     */
    private void putBranchesInQueue(ControlFlowBlock currentBlock, ArrayList<Queue<ControlFlowBlock>> queues) {
        ControlFlowBlock nextBlock1 = currentBlock.getBranches().get(0);
        ControlFlowBlock nextBlock2 = currentBlock.getBranches().get(1);
        Queue<ControlFlowBlock> nextBlocks1 = new LinkedList<>();
        Queue<ControlFlowBlock> nextBlocks2 = new LinkedList<>();
        nextBlocks1.add(nextBlock1);
        nextBlocks2.add(nextBlock2);
        queues.add(nextBlocks1);
        queues.add(nextBlocks2);
    }

    /**
     * Adds the codelines of a block to the ArrayList. If the block is a control structure, the codelines of the blocks
     * between this and the last control structure are sorted. If the control structure is non-conditional, the
     * codelines are not added to the ArrayList.
     * The index of the last control structure is returned.
     * @param block The block with the codelines to be added.
     * @param lines The ArrayList to which the codelines are added.
     * @param indexLastControl The index of the last control structure.
     * @return The index of the last control structure.
     */
    private int addCodelinesToInstructions(ControlFlowBlock block, ArrayList<Integer> lines, int indexLastControl) {
        ArrayList<Integer> codelines = block.getCodelines();
        for(int line : codelines) {
            if(classes.get(line) == LineType.CONTROL_STRUCTURE || classes.get(line) == LineType.CONTROL_STRUCTURE_INFLUENCED_CLASSICAL) {
                // Sort the codelines between the last control structure and this one
                Collections.sort(lines.subList(indexLastControl, lines.size()));
                indexLastControl = instructions.size() - 1;
            }
            if(classes.get(line) != LineType.CONTROL_STRUCTURE) {
                lines.add(line);
            }
        }
        return indexLastControl;
    }
}
