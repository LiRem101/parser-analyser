package de.hhu.lirem101.quil_optimizer.transformation;

import de.hhu.lirem101.quil_optimizer.ExecutableInstructionsExtractor;
import de.hhu.lirem101.quil_optimizer.InstructionNode;

import java.util.ArrayList;
import java.util.Collections;

public class NodeSorter {

    /**
     * Sorts the execution order of the nodes in the instruction list. Ensures that no node is executed before all its
     * dependencies are executed.
     * @param instructionList The list of instructions to sort.
     * @return The sorted list of instructions.
     */
    public static ArrayList<InstructionNode> sortNodes(ArrayList<InstructionNode> instructionList) {
        ArrayList<InstructionNode> newOrder = new ArrayList<>();
        sortNodesWithGivenExecutables(instructionList, newOrder);
        return newOrder;
    }

    /**
     * Sorts nodes from instructionList into executionOrder.
     * @param instructionList The list of instructions to sort.
     * @param executionOrder The list of instructions that are already in the execution queue.
     */
    public static void sortNodesWithGivenExecutables(ArrayList<InstructionNode> instructionList, ArrayList<InstructionNode> executionOrder) {
        boolean newNodes = true;
        instructionList.removeAll(executionOrder);
        while (!executionOrder.containsAll(instructionList) && newNodes && !instructionList.isEmpty()) {
            ExecutableInstructionsExtractor executableExtractor = new ExecutableInstructionsExtractor(new ArrayList<>(Collections.singletonList(instructionList)));
            ArrayList<InstructionNode> executable = executableExtractor.getExecutableInstructionsOfOneBlock(0, executionOrder);
            newNodes = !executable.isEmpty();
            executionOrder.addAll(executable);
            instructionList.removeAll(executable);
        }
    }

}
