package de.hhu.lirem101.quil_optimizer;

import java.util.ArrayList;

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
            ArrayList<InstructionNode> instructionList = instructions.get(i);
            ArrayList<InstructionNode> executionQueueList = executionQueue.get(i);
            ArrayList<InstructionNode> executableInstructions = new ArrayList<>();
            for(InstructionNode node : instructionList) {
                if(!executionQueueList.contains(node) && executionQueueList.containsAll(node.getBranches())) {
                    executableInstructions.add(node);
                }
            }
            executableInstructionsList.add(executableInstructions);
        }
        return executableInstructionsList;
    }
}
