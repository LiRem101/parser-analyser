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
        while (!newOrder.containsAll(instructionList)) {
            ExecutableInstructionsExtractor executableExtractor = new ExecutableInstructionsExtractor(new ArrayList<>(Collections.singletonList(instructionList)));
            ArrayList<InstructionNode> executable = executableExtractor.getExecutableInstructionsOfOneBlock(0, newOrder);
            newOrder.addAll(executable);
            instructionList.removeAll(executable);
        }

        return newOrder;
    }

}
