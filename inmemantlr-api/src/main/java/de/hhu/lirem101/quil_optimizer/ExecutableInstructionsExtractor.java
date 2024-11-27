package de.hhu.lirem101.quil_optimizer;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class ExecutableInstructionsExtractor {
    private final ArrayList<ArrayList<InstructionNode>> instructions;

    public ExecutableInstructionsExtractor(ArrayList<ArrayList<InstructionNode>> instructions) {
        this.instructions = instructions;
    }

    /**
     * Get all instructions of which all previous instructions are already in the execution queue.
     * @param executionQueue The list of list of instructions that are already in the execution queue.
     * @return A list of list of instructions whose previous instructions are already in the execution queue.
     */
    public ArrayList<ArrayList<InstructionNode>> getExecutableInstructions(ArrayList<ArrayList<InstructionNode>> executionQueue) {
        ArrayList<ArrayList<InstructionNode>> executableInstructionsList = new ArrayList<>();
        if(this.instructions.size() != executionQueue.size()) {
            throw new IllegalArgumentException("The size of the instructions list and the executableInstructions list" +
                    " must be the same.");
        }
        for(int i = 0; i < instructions.size(); i++) {
            executableInstructionsList.add(getExecutableInstructionsOfOneBlock(i, executionQueue.get(i)));
        }
        return executableInstructionsList;
    }

    /**
     * Get the executable instructions for a given index.
     * @index The index of the instructions to look at.
     * @param executionQueue The list of instrcutions that are already in the execution queue.
     * @return A list of instructions whose previous instructions are already in the execution queue.
     */
    public ArrayList<InstructionNode> getExecutableInstructionsOfOneBlock(int index, ArrayList<InstructionNode> executionQueue) {
        ArrayList<InstructionNode> instructionList = instructions.get(index);
        ArrayList<InstructionNode> executableInstructions = new ArrayList<>();
        ArrayList<Integer> executedLines = executionQueue.stream().map(InstructionNode::getLine).collect(Collectors.toCollection(ArrayList::new));
        for(InstructionNode node : instructionList) {
            ArrayList<Integer> necessaryLines = node.getBranches().stream().map(InstructionNode::getLine).collect(Collectors.toCollection(ArrayList::new));
            if(!executionQueue.contains(node) && executedLines.containsAll(necessaryLines)) {
                executableInstructions.add(node);
                executedLines.add(node.getLine());
            }
        }
        return executableInstructions;
    }
}
