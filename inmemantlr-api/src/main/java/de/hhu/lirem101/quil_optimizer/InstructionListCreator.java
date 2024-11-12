package de.hhu.lirem101.quil_optimizer;

import de.hhu.lirem101.quil_analyser.ControlFlowBlock;
import de.hhu.lirem101.quil_analyser.LineType;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class InstructionListCreator {
    private final ControlFlowBlock block;
    private final Map<Integer, LineType> classes;
    // A list holding all instructions of the program divided into sections without conditional jumps
    // And sorted by the order of execution
    private ArrayList<ArrayList<InstructionNode>> instructions;
    private final ArrayList<ArrayList<Integer>> linestoJumpTo = new ArrayList<>();
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
            calculateInstructions(allLines);
            calculated = true;
        }
        return copyList(instructions);
    }

    /**
     * Returns a list of lists of integers. Every entry holds the line numbers of the lines the corresponding
     * instruction block in the instructions list jumps to.
     * @return The list of lists of integers.
     */
    public ArrayList<ArrayList<Integer>> getLinesToJumpTo() {
        if (!calculated) {
            ArrayList<ArrayList<Integer>> allLines = calculateExecutionOrder();
            calculateInstructions(allLines);
            calculated = true;
        }
        return copyList(linestoJumpTo);
    }


    /**
     * Copies a list of lists.
     * @param instructions The list of lists to be copied.
     * @return The copied list of lists.
     */
    private <T> ArrayList<ArrayList<T>> copyList(ArrayList<ArrayList<T>> instructions) {
        ArrayList<ArrayList<T>> copy = new ArrayList<>();
        for (ArrayList<T> list : instructions) {
            ArrayList<T> copyList = new ArrayList<>();
            copyList.addAll(list);
            copy.add(copyList);
        }
        return copy;
    }


    /**
     * Calculates lists of instructions from the control flow block.
     */
    private void calculateInstructions(ArrayList<ArrayList<Integer>> allLines) {
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
        instructions = allInstructions;
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
        ArrayList<Integer> currentJumpTos = new ArrayList<>();
        linestoJumpTo.add(currentJumpTos);
        // Queues blocks before the next conditional jump
        Queue<ControlFlowBlock> currentBlocks = new LinkedList<>();
        ArrayList<Queue<ControlFlowBlock>> queues = new ArrayList<>();
        queues.add(currentBlocks);
        Set<Integer> handledJumpTos = new HashSet<>();
        if(!currentBlock.getCodelines().isEmpty()) {
            handledJumpTos.add(currentBlock.getCodelines().get(0));
        }
        int indexOfLastControlStructure = 0;
        while(!queues.isEmpty() && currentBlock != null) {
            boolean branchesConditionally = false;
            boolean handledConditionalJump = false;

            // If currentBlock has codelines, add them to currentLines
            // If the line is a control structure (conditional and non-cond), sort all lines between this control
            // structure and the last one
            if(!currentBlock.getCodelines().isEmpty()) {
                indexOfLastControlStructure = addCodelinesToInstructions(currentBlock, currentLines, indexOfLastControlStructure);
                // If the last line of currentBlock is a conditional jump, add the two succeeding blocks to two next queues
                // If this conditional jump has not already been handled
                int lastLine = currentBlock.getCodelines().get(currentBlock.getCodelines().size() - 1);
                branchesConditionally = classes.get(lastLine) == LineType.CONTROL_STRUCTURE_INFLUENCED_CLASSICAL;
            }


            if(branchesConditionally) {
                putBranchesInQueue(currentBlock, queues, handledJumpTos, currentJumpTos);
                branchesConditionally = false;
            } else {
                // Add branches to current Queue
                currentBlocks.addAll(currentBlock.getBranches());
            }

            // If currentBlocks is empty, remove from queues and take the next block from the next queue
            // Set indexOfLastControlStructure to zero
            if(currentBlocks.isEmpty()) {
                queues.remove(0);
                Collections.sort(currentLines.subList(indexOfLastControlStructure, currentLines.size()));
                if(!queues.isEmpty()) {
                    currentBlocks = queues.get(0);
                    currentBlock = currentBlocks.poll();
                    currentLines = new ArrayList<>();
                    currentJumpTos = new ArrayList<>();
                    allLines.add(currentLines);
                    linestoJumpTo.add(currentJumpTos);
                    indexOfLastControlStructure = 0;
                }
            } else {
                currentBlock = currentBlocks.poll();
            }
        }
        // Indizes of emppty lists
        List<Integer> emptyListIndices = IntStream.range(0, allLines.size())
                .filter(i -> allLines.get(i).isEmpty())
                .boxed()
                .collect(Collectors.toList());

        // Remove empty lists
        allLines.removeIf(ArrayList::isEmpty);
        for(int i = allLines.size()-1; i >= 0; i--) {
            if(emptyListIndices.contains(i)) {
                linestoJumpTo.remove(i);
            }
        }
        return allLines;
    }

    /**
     * Adds the two branches of a block to new queues and add these queues to the list of queues. Olny if the first line
     * of the branch is not handled yet.
     * @param currentBlock The block with the branches to be added.
     * @param queues The queues to which the new queues are added.
     * @param handledJumpTos The set of handled jumps.
     * @param jumpTos The list of lines the array jumps to.
     */
    private void putBranchesInQueue(ControlFlowBlock currentBlock, ArrayList<Queue<ControlFlowBlock>> queues, Set<Integer> handledJumpTos, ArrayList<Integer> jumpTos) {
        ControlFlowBlock nextBlock1 = currentBlock.getBranches().get(0);
        ControlFlowBlock nextBlock2 = currentBlock.getBranches().get(1);
        if(!nextBlock1.getCodelines().isEmpty()) {
            int firstLine1 = nextBlock1.getCodelines().get(0);
            jumpTos.add(firstLine1);
            if(!handledJumpTos.contains(firstLine1)) {
                handledJumpTos.add(firstLine1);
                Queue<ControlFlowBlock> nextBlocks1 = new LinkedList<>();
                nextBlocks1.add(nextBlock1);
                queues.add(nextBlocks1);
            }
        }
        if(!nextBlock2.getCodelines().isEmpty()) {
            int firstLine2 = nextBlock2.getCodelines().get(0);
            jumpTos.add(firstLine2);
            if(!handledJumpTos.contains(firstLine2)) {
                handledJumpTos.add(firstLine2);
                Queue<ControlFlowBlock> nextBlocks2 = new LinkedList<>();
                nextBlocks2.add(nextBlock2);
                queues.add(nextBlocks2);
            }
        }
    }

    /**
     * Adds the codelines of a block to the ArrayList. If the block is a control structure, the codelines of the blocks
     * between this and the last control structure are sorted.
     * The index of the last control structure is returned.
     * @param block The block with the codelines to be added.
     * @param lines The ArrayList to which the codelines are added.
     * @param indexLastControl The index of the last control structure.
     * @return The index of the last control structure.
     */
    private int addCodelinesToInstructions(ControlFlowBlock block, ArrayList<Integer> lines, int indexLastControl) {
        ArrayList<Integer> codelines = block.getCodelines();
        for(int line : codelines) {
            if(!lines.isEmpty() && (classes.get(line) == LineType.CONTROL_STRUCTURE || classes.get(line) == LineType.CONTROL_STRUCTURE_INFLUENCED_CLASSICAL)) {
                // Sort the codelines between the last control structure and this one
                Collections.sort(lines.subList(indexLastControl, lines.size()));
                indexLastControl = lines.size() - 1;
            }
            lines.add(line);
        }
        return indexLastControl;
    }
}
